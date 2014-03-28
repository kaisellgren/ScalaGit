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

import scala.collection.mutable.ListBuffer
import git.util.DataReader

case class TreeEntry(mode: Int, name: String, id: ObjectId)

case class Tree(
  override val id: ObjectId,
  override val header: ObjectHeader,
  entries: Seq[TreeEntry]
) extends Object

object Tree {
  private[git] def decode(bytes: Seq[Byte], id: Option[ObjectId] = None, header: Option[ObjectHeader] = None): Tree = {
    val reader = new DataReader(bytes)

    val entryBuilder = Vector.newBuilder[TreeEntry]

    def parseEntry() {
      val mode = reader.takeStringWhile(_ != ' ').toInt

      reader >> 1 // Space.

      val name = reader.takeStringWhile(_ != 0)

      reader >> 1 // Null.

      entryBuilder += TreeEntry(mode = mode, name = name, id = reader.takeObjectId())
    }

    parseEntry()

    val tree = Tree(
      entries = entryBuilder.result(),
      header = header match {
        case Some(v) => v
        case None => ObjectHeader(ObjectType.Tree, length = bytes.length)
      },
      id = id match {
        case Some(v) => v
        case None => ObjectId("")
      }
    )

    if (id.isDefined) tree
    else tree.copy(id = ObjectId.fromBytes(ObjectDatabase.hashObject(Tree.encode(tree))))
  }

  private[git] def encode(tree: Tree) = {
    val builder = Vector.newBuilder[Byte]

    builder ++= tree.header

    tree.entries.foreach((entry) => {
      builder ++= s"${entry.mode} ${entry.name}\0".getBytes("US-ASCII") ++ entry.id
    })

    builder.result()
  }

  def findById(id: ObjectId)(repository: Repository): Option[Tree] = ObjectDatabase.findObjectById(id)(repository) match {
    case Some(tree: Tree) => Some(tree)
    case _ => None
  }
}