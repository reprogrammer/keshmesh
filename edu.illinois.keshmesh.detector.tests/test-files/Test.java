package p;

public class Test{

 int i; 
 
 public static void main(String args[]){
   Test test = new Test();   
   test.increment();
 }

 public Test(){
   i = 5;
 }

 private void increment(){
   synchronized(getClass()){
     i++;
   }
 }
}
