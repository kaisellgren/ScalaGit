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

/** Represents the chosen strategy for sorting commits.
  *
  * Topological order refers to parents-before-children.
  */
object CommitSortStrategy extends Enumeration {
  type CommitSortStrategy = Value
  val None, Topological, Time, TopologicalTime, Reverse = Value
}