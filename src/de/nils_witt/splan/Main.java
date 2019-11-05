package de.nils_witt.splan;

import com.google.gson.Gson;
import okhttp3.*;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

public class Main {

    public static void main(String[] args) {
        try {
            readXSLX();
        } catch (Exception e){
            e.printStackTrace();
        }
    }


    private static void readXSLX() throws Exception{
        Gson gson = new Gson();
        Integer studentNumber = 0;
        String grade = "";
        Student student;
        InputStream ExcelFileToRead = new FileInputStream("/Users/nilswitt/Downloads/Students.xlsx");
        XSSFWorkbook wb = new XSSFWorkbook(ExcelFileToRead);

        XSSFSheet sheet = wb.getSheetAt(0);
        XSSFRow row;
        XSSFRow secondRow;
        XSSFCell cell;
        XSSFCell nameCell;
        ArrayList<Student> students = new ArrayList<>();
        String lastname;
        String firstname;
        Iterator rows = sheet.rowIterator();

        if(sheet.getRow(2) != null){
            cell = sheet.getRow(2).getCell(1);
            if(cell != null){
                if(cell.getCellType() == CellType.STRING){
                    grade  = cell.getStringCellValue();
                }
            }
        }

        while (rows.hasNext())
        {

            row=(XSSFRow) rows.next();
            cell = row.getCell(0);
            nameCell = row.getCell(1);
            if(cell != null && nameCell != null && rows.hasNext()){

                if(cell.getCellType() == CellType.NUMERIC && nameCell.getCellType() == CellType.STRING) {
                    secondRow = (XSSFRow) rows.next();

                    student = new Student();
                    student.setId((int) cell.getNumericCellValue());
                    lastname = nameCell.getStringCellValue().substring(0,nameCell.getStringCellValue().length()-1);
                    while (lastname.substring((lastname.length() - 1)).equals(" ")){
                        lastname = lastname.substring(0, (lastname.length() - 1));
                    }

                    student.setLastname(lastname);

                    firstname = secondRow.getCell(0).getStringCellValue();
                    while (firstname.substring((firstname.length() - 1)).equals(" ")){
                        firstname = firstname.substring(0, (firstname.length() - 1));
                    }

                    student.setFirstname(firstname);

                    Iterator cells = row.cellIterator();
                    Iterator secondCells = secondRow.cellIterator();
                    Boolean isLK = false;
                    XSSFCell secondCell= null;
                    cells.next();
                    cells.next();
                    secondCells.next();
                    while (cells.hasNext()){
                        cell=(XSSFCell) cells.next();
                        if(secondCells.hasNext()){
                            secondCell = (XSSFCell) secondCells.next();
                        }

                        if (cell.getCellType() == CellType.STRING)
                        {
                            if(!cell.getStringCellValue().equals(" ")){
                                if (secondCell != null) {
                                    if(secondCell.getCellType() == CellType.STRING){
                                        try{
                                            String content = secondCell.getStringCellValue();
                                            if(!content.equals("")){
                                                if(content.substring(0,2).equals("LK")){
                                                    isLK = true;
                                                }
                                            }
                                        }catch (Exception e){
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                Course course = new Course();
                                course.setGrade(grade);
                                String[] parts = cell.getStringCellValue().split(" ");
                                if(parts.length == 2){
                                    if(isLK){
                                        course.setCourseNumber("L"+parts[1]);
                                        isLK = false;
                                    }else{
                                        course.setCourseNumber(parts[1]);
                                    }

                                    course.setSubject(parts[0]);
                                    student.addCourse(course);
                                }

                            }
                        }
                    }
                    students.add(student);
                }
            }
        }

        uploadStudentCourses(students);
    }

    private static void uploadStudentCourses(ArrayList<Student> students){
        Gson gson = new Gson();
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        OkHttpClient client = new OkHttpClient();

        students.forEach(student -> {

            String json = "{\"lastname\":\"" + student.getLastname()+ "\",\"firstname\":\"" + student.getFirstname()+ "\"}";

            RequestBody body = RequestBody.create(JSON, json);
            Request request = new Request.Builder()
                    .url("https://api.nils-witt.codes/users/find")
                    .addHeader("Authorization","Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IndpdHRuaWwxNjExIiwic2Vzc2lvbiI6IjA1OWk4dDBseGk5c3RjMGpreHVidmUiLCJpYXQiOjE1NzI1OTQyOTB9.dEZ9HmEEWEVrWhi2hhh05X5sei5YSnCpuWCDvWX1TxtJrTTPEr1sSYup4djv89cRqX9bZ-tCC1yUP1iq-1ySjP6Aml8EjgnveR8zwXngPC3v85q6mLXf0jR9qVYr4xSGO16h71RzrYa6rAu4IZEqpVEvlOfw6G1BMm6lxpEaB23ZL--LqwOwDha5BjcPXK2OyrNsgADSmRMn-cobIyLh6ab7O5DrdJpxCsPKKGrPEQLtPv6CNiSImM7_dN7VIYqTdPVfFcC0vxxkL3ge2rbN8AmCXn3q7ZgYpYZdV5YTDPrLZE7WJyT07m1UWUPR1XX9RMtgADrlPSKf_KWLdyZZkcTcjpholeaUWT9KSt6x3VdQ3qQPZkBQd3zcLK9VskcFaxB4sCqFxPq-TGOEIpybbca2ioOm8GG6207b2EyQW__B201VxDFQ5X0Xj0_4W6dKg6fwbaG-qehZZIv-zeZ1C-DfY7XqPqd7sooWsfepOo6lj5I1Z_RnCb3txZVxtPC6Ye3TssvOKKML2luUJmIdN5MXAby-IkwMrdCVfESMMlPM3uGuo07o61M89GWWn_GRVkzBQiqEhildRvRk6jDLEjkcq7PflQSIvJyHM0MDiZCd4V2eOah6gqhbHonuLvdFZzzxYJTiIkTWK8WirIXLnZTmihcb3fXWn8iS21M41D4")
                    .post(body)
                    .build();
            try {
                Response response = client.newCall(request).execute();
                try {
                    LdapStudent[] ldapStudents= gson.fromJson(response.body().string(), LdapStudent[].class);
                    if(ldapStudents.length == 1){
                        student.setNmName(ldapStudents[0].getsAMAccountName());
                    }else if(ldapStudents.length == 0){
                        System.out.println("no user found for:" + student.getLastname());
                    }else{
                        System.out.println("Multiple Students found :" + student.getLastname());
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }

            } catch (Exception e){
                e.printStackTrace();
            }

            if(!student.getNmName().equals("")){
                System.out.println("Upload: "+ student.getNmName());

                body = RequestBody.create(JSON, gson.toJson(student.getCourses()));
                request = new Request.Builder()
                        .url("https://api.nils-witt.codes/users/students/" + student.getNmName() + "/setCourses")
                        .addHeader("Authorization","Bearer eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VybmFtZSI6IndpdHRuaWwxNjExIiwic2Vzc2lvbiI6IjA1OWk4dDBseGk5c3RjMGpreHVidmUiLCJpYXQiOjE1NzI1OTQyOTB9.dEZ9HmEEWEVrWhi2hhh05X5sei5YSnCpuWCDvWX1TxtJrTTPEr1sSYup4djv89cRqX9bZ-tCC1yUP1iq-1ySjP6Aml8EjgnveR8zwXngPC3v85q6mLXf0jR9qVYr4xSGO16h71RzrYa6rAu4IZEqpVEvlOfw6G1BMm6lxpEaB23ZL--LqwOwDha5BjcPXK2OyrNsgADSmRMn-cobIyLh6ab7O5DrdJpxCsPKKGrPEQLtPv6CNiSImM7_dN7VIYqTdPVfFcC0vxxkL3ge2rbN8AmCXn3q7ZgYpYZdV5YTDPrLZE7WJyT07m1UWUPR1XX9RMtgADrlPSKf_KWLdyZZkcTcjpholeaUWT9KSt6x3VdQ3qQPZkBQd3zcLK9VskcFaxB4sCqFxPq-TGOEIpybbca2ioOm8GG6207b2EyQW__B201VxDFQ5X0Xj0_4W6dKg6fwbaG-qehZZIv-zeZ1C-DfY7XqPqd7sooWsfepOo6lj5I1Z_RnCb3txZVxtPC6Ye3TssvOKKML2luUJmIdN5MXAby-IkwMrdCVfESMMlPM3uGuo07o61M89GWWn_GRVkzBQiqEhildRvRk6jDLEjkcq7PflQSIvJyHM0MDiZCd4V2eOah6gqhbHonuLvdFZzzxYJTiIkTWK8WirIXLnZTmihcb3fXWn8iS21M41D4")
                        .post(body)
                        .build();
                try {
                    Response response = client.newCall(request).execute();
                } catch (Exception e){
                    e.printStackTrace();
                }
            }

        });
    }
}
