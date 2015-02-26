import neerc.ifmo.AA;

import static neerc.ifmo.AA.PI;

/*
1) obvious name
2) *
3)
 */


public class BB extends AA {
    static class C {
        int y = 0;
        void f(){
            System.err.println(y);
        }
    }

    interface If {
        void run();
    }

    BB.C c = new C();



    int x = 15;

    class A {
        int x = 10;

        void a() {
            System.err.println(x);
            System.err.println(BB.this.x);
            System.err.println(((AA)BB.this).x);
            System.err.println(BB.super.x);
        }
    }

    //anonim class
    void run() {
        final int x = 0;
        // int x = 0; cannot be catched
        new If() {
            @Override
            public void run() {
                System.err.println(x);
            }
        }.run();
    }

    public static void main(String[] args) {
        new BB().run();
    }
}
