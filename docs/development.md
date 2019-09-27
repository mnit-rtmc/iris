# Development

## Building

Install [Fedora] onto the development system.  Some additional development
packages must be installed.
```
dnf install java-devel-openjdk ant-junit git rpm-build gcc cargo
```

In a suitable development directory, clone the iris git repository.
```
git clone https://github.com/mnit-rtmc/iris.git
```

Create lib/ directory in iris development repository.
```
mkdir iris/lib/
```

Download the [JavaMail jar] and copy the `javax.mail.jar` file to the lib/
directory in the iris repository.

Copy `iris-build.properties` from `etc/` to `lib/`.
Edit `lib/iris-build.properties` file as necessary for your organization.

Create the key for signing jar packages.
This is required for Java Web Start, which is used by the IRIS client.
The key should go into your default keystore (`~/.keystore`).
```
keytool -genkeypair -alias signing-key
```

Put the following lines into your `~/.ant.properties` file.
```
debug=off
sign.store=${user.home}/.keystore
sign.store.pass=password
sign.alias=signing-key
sign.alias.pass=signing-password
```

Now, the IRIS rpm file can be built with `ant rpm`.
(The current directory should be the root of the iris repository.

If there are no errors, the new rpm file should be in the
`build/rpm/RPMS/noarch/` directory.

## Contributing

[Bug reports] and feature requests are welcome and encouraged!  Please create an
issue and discuss before making a [pull request].  All patches must be licensed
under the GPL.

## Future Plans

Work is underway on a web-based user interface.  The java based interface will
be fully supported until all features are implemented in the new UI.

## Coding Style

The IRIS coding style is focused on readability.  This is the most important
factor for software maintenance, and also helps new developers who are
unfamiliar with the codebase.

It is recommended that each method fit in one page of text (30 lines by 80
columns).  It is much easier to spot and correct bugs in shorter methods.
Whenever possible, longer methods should be decomposed to abide by this
recommendation.  This allows a developer to quickly read and understand the
logic.

With methods longer than one page, it can be beneficial to have a single exit
point (return).  But for shorter methods (always the goal), this is not so
important, and can reduce readability.  It doesn't make sense to add a local
"ret" variable with a single return statement at the end when the whole method
can be read at a glance.

Each block indent should be 8 columns (using tabs instead of spaces).  This
makes the blocks more obvious.  Always use an indented block with if, for,
while, etc. statements.  This makes it much easier to follow the control flow
than when it is combined onto the same line as the statement.  Don't put an
opening curly brace on a line by itself unless the method declaration or loop
statement spans multiple lines.

Each method should have a javadoc comment describing its purpose.  All
parameters and return values should be documented as well.  Comments within a
method should be used sparingly — never describe what the code is doing, but
instead *why* it was written in a certain way.  Code which is "commented out"
should **never** be committed to a repository.

Unary operators, such as `++` `--` `!` `~` should be adjacent to their operand
with no space.  Binary operators, such as `+` `-` `/` `*` `%` `&` `|` `&&` `||`
`>` `=` `<` `>>` `==` `!=` `<<` `>>>` `>=` `<=` `^` should be separated from
their operands by a space.

## History

**March 1999** — Started work on NTCIP DMS control

**May 2007** — First GPL open source release

**May 2008** — IRIS live in Caltrans D10

**April 2011** — Officially adopted by Caltrans

**November 2014** — Adopted by Wyoming Department of Transportation


[Bug reports]: https://github.com/mnit-rtmc/iris/issues
[Fedora]: http://fedoraproject.org
[JavaMail jar]: https://javaee.github.io/javamail/#Download_JavaMail_Release
[pull request]: https://github.com/mnit-rtmc/iris/pulls
