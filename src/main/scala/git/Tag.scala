/*
 * Copyright (c) 2014 the original author or authors.
 *
 * Licensed under the MIT License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://opensource.org/licenses/MIT
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package git

import java.util.Date
import git.TagType.TagType
import git.util.Parser._
import git.util.{FileUtil, DataReader}
import java.io.File

case class Tag(
  override val id: ObjectId,
  override val header: ObjectHeader,
  name: String,
  message: Option[String],
  taggerName: Option[String],
  taggerEmail: Option[String],
  tagDate: Option[Date],
  tagType: TagType,
  targetIdentifier: ObjectId
) extends Object

object Tag {
  def commit(tag: Tag)(repository: Repository): Option[Commit] = ObjectDatabase.findObjectById(repository, tag.targetIdentifier) match {
    case Some(o: Commit) => Some(o)
    case _ => None
  }

  def find(filter: Option[TagFilter])(repository: Repository): Seq[Tag] = {
    // TODO: We're not searching pack indexes.

    val tagBuffer = Vector.newBuilder[Tag]

    new File(repository.path + Reference.TagPrefix).listFiles().foreach((file: File) => {
      // We read the value inside the tag file to see if it points to a tag, if so, we can add more info about it.
      val tagRef = ObjectId.fromPlain(FileUtil.readContents(file))

      ObjectDatabase.findObjectById(repository, tagRef) match {
        case Some(obj: Tag) => tagBuffer += obj
        case Some(obj: Commit) => tagBuffer += Tag.fromHashCode(ObjectId(FileUtil.readString(file).trim), repository = repository, name = file.getName)
        case _ => throw new Exception(s"Could not find object '${tagRef.sha}', the target of the tag '${file.getName}'")
      }
    })

    tagBuffer.result()
  }

  def find(repository: Repository): Seq[Tag] = find(filter = None)(repository)

  def delete(name: String)(repository: Repository): Unit = {
    try {
      new File(repository.path + Reference.TagPrefix + name).delete()
    } catch {
      case e: java.io.FileNotFoundException => throw new Exception(s"Tag '$name' not found.")
    }

    Cache.deleteTag(repository, name)
  }

  def delete(tag: Tag)(repository: Repository): Unit = delete(tag.name)(repository)

  def create(name: String, targetId: Option[ObjectId])(repository: Repository) = {
    val actualTargetId = targetId match {
      case Some(id: ObjectId) => id
      case None => Repository.head(repository) match {
        case None => throw new Exception("Cannot create a tag without HEAD.")
        case Some(branch: BaseBranch) => branch.tipId
      }
    }

    val file = new File(s"${repository.path}/refs/tags/$name")

    if (file.exists()) throw new Exception(s"Cannot create tag '$name' because it already exists.")

    FileUtil.writeToFile(file, actualTargetId.sha.getBytes.toList)

    Tag(
      id = ObjectId(sha = ""),
      header = ObjectHeader(typ = ObjectType.Tag, length = 0),
      taggerName = None,
      taggerEmail = None,
      tagDate = None,
      message = None,
      name = name,
      tagType = TagType.Lightweight,
      targetIdentifier = actualTargetId
    )
  }

  def toObjectFile(tag: Tag) = ???

  private[git] def fromObjectFile(bytes: Seq[Byte], repository: Repository, id: ObjectId, header: Option[ObjectHeader]): Tag = {
    val reader = new DataReader(bytes)

    if (reader.takeString(7) != "object ") throw new Exception("Corrupted Tag object.")

    // Followed by tag hash.
    val targetIdentifier = reader.takeObjectId()

    reader ++ 1 // LF.

    // Followed by the type.
    if (reader.takeString(5) != "type ") throw new Exception("Corrupted Tag object.")

    val tagType = TagType.withName(reader.takeStringWhile(_ != '\n'))

    reader ++ 1 // LF.

    // The tag type starts with "tag ", also skip.
    if (reader.takeString(4) != "tag ") throw new Exception("Corrupted Tag object.")

    val tagName = reader.takeStringWhile(_ != '\n')

    reader ++ 1 // LF.

    if (reader.takeString(7) != "tagger ") throw new Exception("Corrupted Tag object.")

    val tagger = parseUserFields(reader)

    // Finally the tag message, if it exists.
    val message = Option(reader.getRestAsString)

    Tag(
      id = id,
      header = header match {
        case Some(v) => v
        case None => ObjectHeader(ObjectType.Tag)
      },
      targetIdentifier = targetIdentifier,
      tagType = tagType,
      message = message,
      taggerName = Some(tagger.name),
      taggerEmail = Some(tagger.email),
      tagDate = Some(tagger.date),
      name = tagName
    )
  }

  private[git] def fromHashCode(hashCode: ObjectId, repository: Repository, name: String): Tag = {
    Tag(
      id = hashCode, // TODO: Wrong!
      header = ObjectHeader(ObjectType.Tag),
      tagType = TagType.Lightweight,
      message = None,
      taggerName = None,
      taggerEmail = None,
      tagDate = None,
      name = name,
      targetIdentifier = hashCode // TODO: Wrong!
    )
  }
}

object TagType extends Enumeration {
  type TagType = Value

  val Lightweight = Value("lightweight")
  val Annotated = Value("annotated")
  val Signed = Value("signed")
}