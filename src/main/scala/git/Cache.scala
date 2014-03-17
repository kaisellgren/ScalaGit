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

import scala.concurrent.stm._

case class Cache(
  tags: Ref[Vector[Tag]] = Ref(Vector()),
  branches: Ref[Vector[BaseBranch]] = Ref(Vector()),
  references: Ref[Vector[Reference]] = Ref(Vector())
)

object Cache {
  def clearTags(repository: Repository) = atomic { implicit txn =>
    repository.cache.tags() = Vector()
  }

  def clearBranches(repository: Repository) = atomic { implicit txn =>
    repository.cache.branches() = Vector()
  }

  def clearReferences(repository: Repository) = atomic { implicit txn =>
    repository.cache.references() = Vector()
  }

  def clear(repository: Repository) {
    clearTags(repository)
    clearBranches(repository)
    clearReferences(repository)
  }

  private[git] def deleteTag(repository: Repository, name: String) = atomic { implicit txn =>
    repository.cache.tags() = repository.cache.tags().filterNot(_.name == name)
  }
}