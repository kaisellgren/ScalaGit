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
import org.joda.time.DateTime

class TagSpec extends FlatSpec with Matchers {
  // Create an example tag.
  val tag = Tag(
    id = ObjectId("94190d9416dcec7fff3950d017b45edb37ab6420"),
    header = ObjectHeader(ObjectType.Tag),
    name = "foo bar baz",
    message = Some("foo!"),
    taggerName = Some("Kai"),
    taggerEmail = Some("kaisellgren@gmail.com"),
    tagDate = Some(new DateTime(2014, 1, 2, 3, 4, 5).toDate),
    tagType = TagType.Annotated,
    targetIdentifier = ObjectId("31382bef9edff398e699822d1a49925159b3a26c")
  )

  "an encoded tag" should "decode back to itself" in {
    val tag2 = Tag.decode(Tag.encode(tag))

    tag2.id.sha shouldBe tag.id.sha
    tag2.header.typ shouldBe tag.header.typ
    tag2.name shouldBe tag.name
    tag2.message shouldBe tag.message
    tag2.taggerName shouldBe tag.taggerName
    tag2.taggerEmail shouldBe tag.taggerEmail
    tag2.tagDate shouldBe tag.tagDate
    tag2.tagType shouldBe tag.tagType
    tag2.targetIdentifier shouldBe tag.targetIdentifier
  }

  // Testing fixtures.
  val tag2 = Tag.decode(FileUtil.readContents(new File("src/test/resources/objects/tag/94190d9416dcec7fff3950d017b45edb37ab6420")))

  "tag 94190d9416dcec7fff3950d017b45edb37ab6420" should "have an ID of '94190d9416dcec7fff3950d017b45edb37ab6420'" in {
    tag2.id.sha shouldBe "94190d9416dcec7fff3950d017b45edb37ab6420"
  }

  it should "be type of Tag" in {
    tag2.header.typ shouldBe ObjectType.Tag
  }

  it should "have the right message" in {
    tag2.message shouldBe Some("foo!")
  }

  it should "be named 'foo bar baz'" in {
    tag2.name shouldBe "foo bar baz"
  }

  it should "have the right tagger details" in {
    tag2.taggerName shouldBe Some("Kai")
    tag2.taggerEmail shouldBe Some("kaisellgren@gmail.com")
  }

  it should "date back to 1st Feb 2014, 03:04:05 o'clock" in {
    tag2.tagDate shouldBe Some(new DateTime(2014, 1, 2, 3, 4, 5).toDate)
  }
}