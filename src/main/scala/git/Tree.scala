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

import git.util.DataReader

case class TreeEntry(mode: Int, name: String, id: ObjectId)

case class Tree(
  override val id: ObjectId,
  override val header: ObjectHeader,
  entries: Seq[TreeEntry]
) extends Object

object Tree {
  /** Returns the tree encoded as a sequence of bytes. */
  private[git] def encode(tree: Tree): Seq[Byte] = {
    val builder = Vector.newBuilder[Byte]

    val body = Tree.encodeBody(tree)
    val header = if (tree.header.length > 0) tree.header else tree.header.copy(length = body.length)

    builder ++= header
    builder ++= body

    builder.result()
  }

  /** Returns the tree body encoded as a sequence of bytes. */
  private[git] def encodeBody(tree: Tree): Seq[Byte] = {
    val builder = Vector.newBuilder[Byte]

    tree.entries.foreach((entry) => {
      builder ++= s"${entry.mode} ${entry.name}\0"
      builder ++= entry.id
    })

    builder.result()
  }

  /** Returns the bytes decoded as a Tree. */
  private[git] def decode(bytes: Seq[Byte]): Tree = {
    val header = ObjectHeader.decode(bytes)
    val data = bytes.takeRight(header.length)

    decodeBody(data, id = None, header = Some(header))
  }

  /** Returns the bytes decoded as a Tree body. */
  private[git] def decodeBody(bytes: Seq[Byte], id: Option[ObjectId] = None, header: Option[ObjectHeader] = None): Tree = {
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
      header = header.getOrElse(ObjectHeader(ObjectType.Tree)),
      id = id.getOrElse(ObjectId(""))
    )

    if (id.isDefined) tree
    else tree.copy(id = ObjectId.decode(ObjectDatabase.hashObject(Tree.encode(tree))))
  }

  /** Returns the Tree for the given ID as [[Option]]. */
  def findById(id: ObjectId)(repository: Repository): Option[Tree] = ObjectDatabase.findObjectById(id)(repository) match {
    case Some(tree: Tree) => Some(tree)
    case _ => None
  }
}