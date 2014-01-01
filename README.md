Rekord
======

An ORM for Java with intelligent loading and caching

# TODO

#### Features to Implement
- Validation
- Initializers
  - Sequences
  - Encrypted Fields
  - Salted Encrypted Fields
- ~~Partial Selection (like the first 100 characters of a text field)~~
- ~~Application Caching (opposed to transaction caching)~~
- Native Queries

#### Simple Changes
- Model has BitSet of set values to quickly know that it already has a view
- View has BitSet of fields (based on their indices)
- ~~Caching options in Configuration~~
- Import XML option
- Many-to-Many field
- Table Extensions (no way to know what the implementation is)
- Table Polymorphism (distinction field)
- Generic Pointer (distinction field, key)
- Improve LazyList performance
- Outer-Join fetching for one-to-one
- Cascade Save option [default=true]
- Schema option (global and table level) [default depends on database implementation]
- Catalog option (global and table level) [default depends on database implementation]
- ~~Dynamic-Insert option (if a field is given, supply it even if it's generated)~~
- Dynamic-Update option (only update what has changed)
- SelectQuery.list returns LazyList
- Select-Before-Update (ensure a change hasn't occurred since when the model was loaded)
- ~~Formula Fields~~
- Saving Order?

#### Other Changes
- Move all SQL to one place, make Transaction in charge of making all queries
- Only insert into history if one of the history columns will be updated
- Only create and save OneToOne model if the field was selected OR it's an insert