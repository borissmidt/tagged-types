# tagged-types

[![Build status](https://img.shields.io/travis/Treev-io/tagged-types/master.svg)](https://travis-ci.org/Treev-io/tagged-types)
[![Download](https://api.bintray.com/packages/treevio/maven/tagged-types/images/download.svg)](https://bintray.com/treevio/maven/tagged-types/_latestVersion)

Zero-dependency boilerplate-free tagged types for Scala.

- [tagged-types](#tagged-types)
   - [Usage](#usage)
     - [`sbt`](#sbt)
     - [API](#api)
       - [Defining tagged types](#defining-tagged-types)
       - [Tagging](#tagging)
         - [Tagging values](#tagging-values)
         - [Tagging container values](#tagging-container-values)
         - [Adding more tags](#adding-more-tags)
   - [Migrating from value classes](#migrating-from-value-classes)
     - [Note about implicit resolution](#note-about-implicit-resolution)

## Usage

### `sbt`

Add the following to your `build.sbt`:

```scala
resolvers += Resolver.bintrayRepo("treevio", "maven")

libraryDependencies += "io.treev" %% "tagged-types" % "1.3"
```

Artifacts are published both for Scala `2.11` and `2.12`.

### API

#### Defining tagged types

```scala
import io.treev.tag._

object username extends TaggedType[String]
```

It's helpful to define a type alias for convenience:

```scala
object username extends TaggedType[String] { type Username = Type }
```

`TaggedType` provides the following members:

* `apply` method to construct tagged type from raw values, e.g. `username("scooper")`;
* `Tag` trait to access the tag, e.g. `List("scooper").@@@[username.Tag]` (see below for container tagging);
* `Raw` type member to access raw type, e.g. to help with type inference where needed:

```scala
object username extends TaggedType[String] { type Username = Type }

import username.Username
case class User(name: Username)

val users = List(User(username("scooper")))
users.sortBy(_.name: username.Raw)
```

* `Type` type member to access tagged type.

#### Tagging

##### Tagging values

```scala
sealed trait UsernameTag

val sheldon = "scooper".@@[UsernameTag]
// or val sheldon = "scooper".taggedWith[UsernameTag]
// sheldon: String @@ UsernameTag
```

Or, if you have `TaggedType` instance:

```scala
object username extends TaggedType[String]

val sheldon = "scooper" @@ username 
// or val sheldon = "scooper" taggedWith username
// or val sheldon = username("scooper")
// sheldon: String @@ username.Tag
```

##### Tagging container values

```scala
val rawUsers = List("scooper", "lhofstadter", "rkoothrappali")
val users = rawUsers.@@@[UsernameTag]
// or val users = rawUsers.taggedWithF[UsernameTag]
// users: List[String @@ UsernameTag]
```

Can also tag using `TaggedType` instance as above.

##### Adding more tags

Immediate value:

```scala
sealed trait OwnerTag

val username = "scooper".@@[UsernameTag]
val owner = username.+@[OwnerTag]
// or val owner = username.andTaggedWith[OwnerTag]
// owner: String @@ (UsernameTag with OwnerTag)
```

Container value:

```scala
val owners = users.+@@[OwnerTag]
// or val owners = users.andTaggedWithF[OwnerTag]
// owners: List[String @@ (UsernameTag with OwnerTag)]
```

Can also tag using `TaggedType` instance as above.

## Migrating from value classes

Suppose you have a value class:

```scala
case class Username(value: String) extends AnyVal {
  def isValid: Boolean = !value.isEmpty
}
object Username {
  val FieldName: String = "Username"
  
  implicit val ordering: Ordering[Username] = Ordering.by(_.value)
}
```

Then, it's a matter of changing it to:

```scala
object username extends TaggedType[String]
```

Any methods on original case class instance turn into implicit extensions:

```scala
object username extends TaggedType[String] {
  implicit class UsernameExtensions(val value: Type) 
    extends AnyVal { // still good application of value classes
  
    def isValid: Boolean = !value.isEmpty
  }
}
```

Any constants on original case class' companion object are merged into `username` object:

```scala
object username extends TaggedType[String] {
  val FieldName: String = "Username"
  
  implicit val ordering: Ordering[Type] = Ordering[String].@@@[Tag]
}
```

### Note about implicit resolution

Implicit resolution won't work as it was before when using companion objects, so, to bring implicit `Ordering` instance or `UsernameExtensions` from above into scope, need to import it explicitly:

```scala
import username._
// or import username.ordering
// or import username.UsernameExtensions
```
