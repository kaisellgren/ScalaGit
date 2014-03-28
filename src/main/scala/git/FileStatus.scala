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

object FileStatus {
  val Nonexistent = 1 << 32
  val Current = 0 // Unaltered.
  val New = 1 << 0
  val Staged = 1 << 1
  val Deleted = 1 << 2
  val Renamed = 1 << 3
  val IndexTypeChange = 1 << 4 // Index type change.
  val Untracked = 1 << 7
  val Modified = 1 << 8
  val Missing = 1 << 9
  val TypeChanged = 1 << 10
  val Ignored = 1 << 14

  // Helpers for the lazy people.
  def isCurrent(s: Int): Boolean = (s & Current) != 0
  def isNew(s: Int): Boolean = (s & New) != 0
  def isStaged(s: Int): Boolean = (s & Staged) != 0
  def isDeleted(s: Int): Boolean = (s & Deleted) != 0
  def isRenamed(s: Int): Boolean = (s & Renamed) != 0
  def isUntracked(s: Int): Boolean = (s & Untracked) != 0
  def isModified(s: Int): Boolean = (s & Modified) != 0
  def isMissing(s: Int): Boolean = (s & Missing) != 0
  def isIgnored(s: Int): Boolean = (s & Ignored) != 0

  def isInStagingArea(s: Int) = isStaged(s) || isRenamed(s) || isDeleted(s) || isModified(s)
}