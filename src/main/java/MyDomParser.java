import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MyDomParser {
    public static void main(String[] args) throws ParserConfigurationException, IOException, SAXException, IllegalAccessException, ClassNotFoundException, SQLException {

        //Parsing the xml file and storing the data into a list
        SameThing sameThing = new SameThing();
        List<Employee> employeeList = new ArrayList<Employee>();
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse("src/main/java/dataset2.xml");
        NodeList empList = document.getElementsByTagName("Employee");
        for(int i = 0; i < empList.getLength(); i++){
            Employee employee = new Employee();
            Node node = empList.item(i);
            if(node.getNodeType() == Node.ELEMENT_NODE){
                Element element = (Element) node;
                Class<?> clap = Employee.class;
                for(Field field: clap.getDeclaredFields()){
                    if(field.getType().equals(int.class)){
                        continue;
                    }
                    field.setAccessible(true);
                    String value;
                    if(field.isAnnotationPresent(NameToName.class)){
                        value = sameThing.whatever(field);
                    }else{
                        value = field.getName();
                    }
                    NodeList haha = element.getElementsByTagName(value);
                    Node node1 = haha.item(0);
                    Element element1 = (Element) node1;
                    field.set(employee, element1.getTextContent());
                }
            }
            employeeList.add(employee);
            System.out.println(employee.toString());
        }


        //Creating a JDBC connection and storing the values
        String url = "jdbc:postgresql://localhost:5432/EmployeesManager";
        String userName = "postgres";
        String password = "Kyouma#001";
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection(url, userName, password);

        for(Employee emp : employeeList){
            //String query = "INSERT INTO employees (first_name, last_name, email, gender, password) VALUES (?,?,?,?,?)";
            Class<?> clap = Employee.class;
            StringBuffer query = new StringBuffer();
            query.append("INSERT INTO employees (");
            for(Field field: clap.getDeclaredFields()){
                field.setAccessible(true);
                if(field.getName().equals("id")){
                    continue;
                }
                if(field.isAnnotationPresent(NameToName.class)){
                    query.append(field.getAnnotation(NameToName.class).value()).append(",");
                }else{
                    query.append(field.getName()).append(",");
                }
            }
            query.deleteCharAt(query.length()-1);
            query.append(") VALUES (");
            for(Field field: clap.getDeclaredFields()){
                field.setAccessible(true);
                if(field.getName().equals("id")){
                    continue;
                }
                query.append("'").append(field.get(emp)).append("'").append(",");
            }
            query.deleteCharAt(query.length()-1);
            query.append(");");


            PreparedStatement preparedStatement = con.prepareStatement(query.toString());
            preparedStatement.executeUpdate();
        }
    }
}
