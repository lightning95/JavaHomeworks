package MyExceptions;

/**
 * Created by Aydar on 15.04.14.
 */
public abstract class EvaluateException extends Exception{
    final private String message;

    public EvaluateException(String message) {
       this.message = message;
    }

    public String getMessage(){
        return message;
    }
}
