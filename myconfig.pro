-injars build/libs/novello-0.01-SNAPSHOT.jar
-outjars build/libs/novello-0.01-SNAPSHOT-pg.jar
-libraryjars <java.home>/lib/rt.jar
-libraryjars  C:\Users\Chris\.m2\repository\org\apache\ant\ant\1.8.1

-keep public class com.welty.nboard.nboard.NBoard {
    public static void main(java.lang.String[]);
}