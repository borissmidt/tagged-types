package taggedtypes.typeclass

import taggedtypes.@@

trait AnyTypeclassTaggingCompat {

  implicit def liftAnyTypeclass[Typeclass[_], T, Tag](implicit tc: Typeclass[T]): Typeclass[T @@ Tag] =
    tc.asInstanceOf[Typeclass[T @@ Tag]]

}
