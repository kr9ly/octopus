# Octopus - Minimal EventBus based on Annotation Processing

[![Circle CI](https://circleci.com/gh/kr9ly/octopus/tree/master.svg?style=shield)](https://circleci.com/gh/kr9ly/octopus/tree/master)

- [x] Type based Event Dispatch
- [x] Support Event Propagation (child to parent)
- [x] Java6+ Support
- [x] No Runtime Reflection API call
- [x] Proguard friendly

# Dependency

Add JCenter Dependency to `repositories` block in your build.gradle

```groovy
repositories {
    jcenter()
}
```

And Add this to `dependencies` block in your build.gradle

```groovy
dependencies {
    compile 'net.kr9ly:octopus:1.1.0'
    annotationProcessor 'net.kr9ly:octopus-compiler:1.1.0'
}
```

# Usage

### Basic Usage

```java
class Main {
    
    private static class Event {}
    
    private static class EventHandlers {
        
        @net.kr9ly.octopus.Callback
        public void handleEvent(Event event) {
            System.out.println("fire!");
        }
    }
    
    public static void main(String[] args) {
        net.kr9ly.octopus.Octopus octopus = new net.kr9ly.octopus.Octopus();
        octopus.register(new EventHandlers());
        octopus.post(new Event()); // fire!
    }
}
```

### Event Propagation

```java
class Main {
    
    private static class ChildEvent {}
    
    private static class ParentEvent {}
    
    private static class ChildHandlers {
        
        @net.kr9ly.octopus.Callback
        public void handleChildEvent(ChildEvent event) {
            System.out.println("child");
        }
    }
    
    private static class ParentHandlers {
        
        @net.kr9ly.octopus.Callback
        public boolean handleParentEvent(ParentEvent event) {
            System.out.println("parent");
        }
    }
    
    public static void main(String[] args) {
        net.kr9ly.octopus.Octopus parent = new net.kr9ly.octopus.Octopus();
        net.kr9ly.octopus.Octopus child = new net.kr9ly.octopus.Octopus(parent);
        child.register(new ChildHandlers());
        parent.register(new ParentHandlers());
        child.post(new ChildEvent()); // child
        child.post(new ParentEvent()); // parent
    }
}
```


### Prevent Propagation

```java
class Main {
    
    private static class Event {}
    
    private static class Event2 {}
    
    private static class ChildHandlers {
        
        @net.kr9ly.octopus.Callback
        public void doPropagation(Event event) {
            System.out.println("child");
        }
        
        @net.kr9ly.octopus.Callback
        public boolean preventPropagation(Event2 event) {
            System.out.println("child");
            return true; // return true for prevent propagation
        }
    }
    
    private static class ParentHandlers {
        
        @net.kr9ly.octopus.Callback
        public boolean handleEvent(Event event) {
            System.out.println("parent");
        }
        
        @net.kr9ly.octopus.Callback
        public boolean handleEvent(Event2 event) {
            System.out.println("parent");
        }
    }
    
    public static void main(String[] args) {
        net.kr9ly.octopus.Octopus parent = new net.kr9ly.octopus.Octopus();
        net.kr9ly.octopus.Octopus child = new net.kr9ly.octopus.Octopus(parent);
        child.register(new ChildHandlers());
        parent.register(new ParentHandlers());
        child.post(new Event()); // "child", "parent"
        child.post(new Event2()); // only "child"
    }
}
```

### Handle Exception

```java
class Main {
    
    private static class Event {}
    
    private static class ChildHandlers {
        
        @net.kr9ly.octopus.Callback
        public void handleEvent(Event event) throws Throwable {
            throw new Exception();
        }
    }
    
    private static class MyExceptionHandler implements net.kr9ly.octopus.ExceptionHandler {
        
        @net.kr9ly.octopus.Callback
        public boolean handle(Throwable throwable) {
            System.out.println("found exception!");
            return true; // return true for prevent propagation
        }
    }
    
    public static void main(String[] args) {
        net.kr9ly.octopus.Octopus parent = new net.kr9ly.octopus.Octopus(new MyExceptionHandler());
        net.kr9ly.octopus.Octopus child = new net.kr9ly.octopus.Octopus(parent);
        child.register(new ChildHandlers());
        child.post(new Event()); // found exception!
    }
}
```

### Proguard

```proguard
-keep class * extends net.kr9ly.octopus.internal.CallbacksFinder
```

# License

```
Copyright 2017 kr9ly

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```