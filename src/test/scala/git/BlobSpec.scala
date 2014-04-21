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

class BlobSpec extends FlatSpec with Matchers {
  // Create an example blob.
  val data = "foo bar baz".getBytes("US-ASCII")

  val blob = Blob(
    id = ObjectId("31382bef9edff398e699822d1a49925159b3a26c"),
    header = ObjectHeader(ObjectType.Blob),
    size = data.length,
    contents = data
  )

  "an encoded blob" should "decode back to itself" in {
    val blob2 = Blob.decode(Blob.encode(blob))

    blob2.id.sha should be (blob.id.sha)
    blob2.header.typ should be (blob.header.typ)
    blob2.size should be (blob.size)
    blob2.contents should be (blob.contents)
  }

  // Testing fixtures.
  val blob2 = Blob.decode(FileUtil.readContents(new File("src/test/resources/objects/blob/31382bef9edff398e699822d1a49925159b3a26c")))

  "blob 31382bef9edff398e699822d1a49925159b3a26c" should "have an ID of '31382bef9edff398e699822d1a49925159b3a26c'" in {
    blob2.id.sha shouldBe "31382bef9edff398e699822d1a49925159b3a26c"
  }

  it should "be type of Blob" in {
    blob2.header.typ shouldBe ObjectType.Blob
  }

  it should "have a size of 11" in {
    blob2.size shouldBe 11
  }

  it should "have expected contents" in {
    blob2.contents should be ("foo bar baz".getBytes("US-ASCII"))
  }
}