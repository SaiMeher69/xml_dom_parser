import java.lang.reflect.Field;

public class SameThing {
    public String whatever(Field field){
        if(field.isAnnotationPresent(NameToName.class)){
            return field.getAnnotation(NameToName.class).value();
        }else{
            return field.getName();
        }
    }
}
