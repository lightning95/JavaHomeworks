mkdir bin
javac -d bin src/ru/ifmo/ctddev/gizatullin/implementor/Implementor.java
mkdir /src/META-INF
cd bin
jar cfm Implementor.jar ../src/META-INF/MANIFEST.MF