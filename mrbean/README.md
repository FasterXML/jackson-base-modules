# Mr Bean - Bean Materializer for Jackson

Mr Bean is an extension that implements support for "POJO type materialization";
ability for [databinder](jackson-databind) to construct implementation classes for Java interfaces and abstract classes, as part of deserialization.
Extension plugs in using standard `Module` interface, and requires Jackson 2.0 or above.

Module is licensed under [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0.txt)

## Usage

### Maven dependency

To use module on Maven-based projects, use following dependency:

```xml
<dependency>
  <groupId>tools.jackson.module</groupId>
  <artifactId>jackson-module-mrbean</artifactId>
  <version>3.0.0-SNAPSHOT</version>
</dependency>
```

(or whatever version is most up-to-date at the moment)

## Usage

### Registering module

To use the the Module in Jackson, simply register it with the ObjectMapper instance:

```java
ObjectMapper mapper = JsonMapper.builder()
// tools.jackson.module.mrbean.MrBeanModule:
    .addModule(new MrBeanModule())
    .build();
```

### Simple usage

Once module is registered, all you need is an interface like:

```java
public interface Point {
  // may have setters and/or getters
  public int getX();
  public void setX(int value);
  // but setters are optional if getter exists:
  public int getY();
}
```

and then you can read JSON into an implementation class of given interface or abstract class:

```java
String json = "{\"x\":12,\"y\":35}";
Point p = objectMapper.readValue(json, Point.class);
```

(to contrast, try running this example without module registration -- this would result in an exception being thrown)

Note: this works transitively as well, meaning that implementation classes will be materialized for any interface and abstract types.

## Implementation details

### How does it work?

Module implements Jackson's `AbstractTypeResolver` interface and registers it via Module interface. This is how it gets invoked when an abstract type is encountered, which is problematic for deserialization (unless polymorphic handling is enabled; see the next section).

Actual byte code generation uses [ASM](http://asm.ow2.org/) library, and simply generates methods for all abstract methods.
No naming convention is required; basic signature is enough. This means that zero-argument methods are considered "getters" and one-argument methods "setters".
Naming is considered to be able to generate internal field; but beyond this naming is of no consequence.
Annotations are not introspected at this point; however, Jackson databinding will make use of them later on -- typically annotations from implemented interface or abstract class get used, as no annotations are added to the implementation classes.

### Issue: incompatibility with polymorphic types

One potential area of conflict is that of handling of so-called polymorphic types (POJOs annotated with `@JsonTypeInfo` annotation).
Since base classes are often abstract classes, but those classes should not be materialized, because they are never used (instead, actual concrete sub-classes are used).
Because of this, Mr Bean will ''not materialize any types annotated with @JsonTypeInfo annotation''.

Another potential concern is that of "default typing", in which case Jackson would use polymorphic type handling for wider set of types.
Use of Mr Bean is not recommended together with enabling of "default typing", since either mr Bean or default typing is not going to work correctly.
