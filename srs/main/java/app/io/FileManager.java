package io;

import model.*;
import util.IdGenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.PriorityQueue;

import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.xml.sax.InputSource;

/**
 * Класс для работы с файлом (чтение/запись XML).
 */

public class FileManager {

    private final String filename;

    public FileManager(String filename) {
        this.filename = filename;
    }

    public PriorityQueue<StudyGroup> load() {
        PriorityQueue<StudyGroup> collection = new PriorityQueue<>();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(filename), StandardCharsets.UTF_8)) {

            // Читаем весь файл в строку
            StringBuilder xmlContent = new StringBuilder();
            int data;
            while ((data = reader.read()) != -1) {
                xmlContent.append((char) data);
            }

            // Создаем парсер
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document document = builder.parse(new InputSource(new StringReader(xmlContent.toString())));

            document.getDocumentElement().normalize();

            NodeList nodeList = document.getElementsByTagName("studyGroup");

            for (int i = 0; i < nodeList.getLength(); i++) {

                Element element = (Element) nodeList.item(i);

                StudyGroup group = parseStudyGroup(element);

                collection.add(group);

                // обновляем id генератор
                IdGenerator.updateCurrentId(group.getId());
            }

        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден. Будет создан новый.");
        } catch (Exception e) {
            System.out.println("Ошибка при загрузке файла: " + e.getMessage());
        }

        return collection;
    }

    
    public void save(PriorityQueue<StudyGroup> collection) {

        try (FileOutputStream fos = new FileOutputStream(filename)) {

            String xml = convertToXml(collection);
            fos.write(xml.getBytes());

        } catch (IOException e) {
            System.out.println("Ошибка записи файла: " + e.getMessage());
        }
    }

    private String convertToXml(PriorityQueue<StudyGroup> collection) {

        StringBuilder sb = new StringBuilder();
        sb.append("<studyGroups>\n");

        for (StudyGroup group : collection) {

            sb.append("  <studyGroup>\n");

            sb.append("    <id>").append(group.getId()).append("</id>\n");
            sb.append("    <name>").append(group.getName()).append("</name>\n");
            sb.append("    <creationDate>").append(group.getCreationDate()).append("</creationDate>\n");
            sb.append("    <studentsCount>").append(group.getStudentsCount()).append("</studentsCount>\n");

            if (group.getExpelledStudents() != null) {
                sb.append("    <expelledStudents>")
                .append(group.getExpelledStudents())
                .append("</expelledStudents>\n");
            } 
            else {
                sb.append("    <expelledStudents></expelledStudents>\n");
            }

            sb.append("    <transferredStudents>")
            .append(group.getTransferredStudents())
            .append("</transferredStudents>\n");

            if (group.getSemesterEnum() != null) {
                sb.append("    <semesterEnum>")
                .append(group.getSemesterEnum())
                .append("</semesterEnum>\n");
            } 
            else {
                sb.append("    <semesterEnum></semesterEnum>\n");
            }

            // Coordinates
            sb.append("    <coordinates>\n");
            sb.append("      <x>").append(group.getCoordinates().getX()).append("</x>\n");
            sb.append("      <y>").append(group.getCoordinates().getY()).append("</y>\n");
            sb.append("    </coordinates>\n");

            // Person
            sb.append("    <groupAdmin>\n");
            sb.append("      <name>").append(group.getGroupAdmin().getName()).append("</name>\n");
            sb.append("      <birthday>")
            .append(group.getGroupAdmin().getBirthday().getTime())
            .append("</birthday>\n");

            if (group.getGroupAdmin().getEyeColor() != null) {
                sb.append("      <eyeColor>")
                .append(group.getGroupAdmin().getEyeColor())
                .append("</eyeColor>\n");
            } 
            else {
                sb.append("      <eyeColor></eyeColor>\n");
            }

            if (group.getGroupAdmin().getNationality() != null) {
                sb.append("      <nationality>")
                .append(group.getGroupAdmin().getNationality())
                .append("</nationality>\n");
            } 
            else {
                sb.append("      <nationality></nationality>\n");
            }

            sb.append("    </groupAdmin>\n");

            sb.append("  </studyGroup>\n");
        }

        sb.append("</studyGroups>");

        return sb.toString();
    }


    private StudyGroup parseStudyGroup(Element element) {

        Integer id = Integer.parseInt(getTagValue(element, "id"));
        String name = getTagValue(element, "name");

        int studentsCount = Integer.parseInt(getTagValue(element, "studentsCount"));
        int transferredStudents = Integer.parseInt(getTagValue(element, "transferredStudents"));

        Long expelledStudents = null;
        String expelledStr = getTagValue(element, "expelledStudents");

        if (expelledStr != null && !expelledStr.isEmpty()) {
            expelledStudents = Long.parseLong(expelledStr);
        }

        Semester semester = null;
        String semesterStr = getTagValue(element, "semesterEnum");
        if (semesterStr != null && !semesterStr.isEmpty()) {
            semester = Semester.valueOf(semesterStr);
        }

        

        LocalDate creationDate = LocalDate.parse(getTagValue(element, "creationDate"));

        // Coordinates
        Element coordinatesElement = (Element) element.getElementsByTagName("coordinates").item(0);
        int x = Integer.parseInt(getTagValue(coordinatesElement, "x"));
        Double y = Double.parseDouble(getTagValue(coordinatesElement, "y"));
        Coordinates coordinates = new Coordinates(x, y);

        // Person
        Element personElement = (Element) element.getElementsByTagName("groupAdmin").item(0);
        String personName = getTagValue(personElement, "name");
        Date birthday = new Date(Long.parseLong(getTagValue(personElement, "birthday")));

        Color eyeColor = null;
        String eyeStr = getTagValue(personElement, "eyeColor");
        if (eyeStr != null && !eyeStr.isEmpty()) {
            eyeColor = Color.valueOf(eyeStr);
        }

    Country nationality = null;
    String natStr = getTagValue(personElement, "nationality");
    if (natStr != null && !natStr.isEmpty()) {
        nationality = Country.valueOf(natStr);
    }

    Person admin = new Person(personName, birthday, eyeColor, nationality);

        return new StudyGroup(id, name, coordinates, creationDate, studentsCount, expelledStudents, transferredStudents, semester, admin);
    }

    private String getTagValue(Element element, String tagName) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list.getLength() == 0) return null;
        Node node = list.item(0);
        return node.getTextContent();

    }

}


