package git

// TODO: Consider renaming... git.GitObject?
class Object {
  var id: ObjectId = _
  var header: ObjectHeader = _
  var repository: Repository = _
}