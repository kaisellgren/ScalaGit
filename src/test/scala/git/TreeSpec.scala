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

import org.scalatest.{Matchers, FlatSpec}
import java.io.File
import git.util.FileUtil

class TreeSpec extends FlatSpec with Matchers {
  // Create an example tree.
  val tree = Tree(
    id = ObjectId("bbfe24b66b27bec23a9ce75c5902f747b21207f2"),
    header = ObjectHeader(ObjectType.Tree),
    entries = Seq(TreeEntry(mode = 1, name = "foo", id = ObjectId("31382bef9edff398e699822d1a49925159b3a26c")))
  )

  "an encoded tree" should "decode back to itself" in {
    val tree2 = Tree.decode(Tree.encode(tree))

    tree2.id.sha shouldBe tree.id.sha
    tree2.header.typ shouldBe tree.header.typ
    tree2.entries shouldBe tree.entries
  }

  // Testing fixtures.
  val tree2 = Tree.decode(FileUtil.readContents(new File("src/test/resources/objects/tree/bbfe24b66b27bec23a9ce75c5902f747b21207f2")))

  "tree bbfe24b66b27bec23a9ce75c5902f747b21207f2" should "have an ID of 'bbfe24b66b27bec23a9ce75c5902f747b21207f2'" in {
    tree2.id.sha shouldBe "bbfe24b66b27bec23a9ce75c5902f747b21207f2"
  }

  it should "be type of Tree" in {
    tree2.header.typ shouldBe ObjectType.Tree
  }

  it should "have expected entries" in {
    tree2.entries.length shouldBe 1
    tree2.entries should contain (TreeEntry(mode = 1, name = "foo", id = ObjectId("31382bef9edff398e699822d1a49925159b3a26c")))
  }
}