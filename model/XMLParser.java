package model;

import javax.xml.parsers.DocumentBuilderFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

public class XMLParser {
    // builds document from XML file
    public Document getDocFromFile(String filename) throws ParserConfigurationException {

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = dbf.newDocumentBuilder();
        Document doc = null;

        try {
            doc = db.parse(filename);
        } catch (Exception ex) {
            System.out.println("XML parse failure");
            ex.printStackTrace();
        }

        return doc;

    }

    // reads data as SceneCard objects, returns list of all scene cards
    public List<SceneCard> readCards(Document d) {

        List<SceneCard> sceneCards = new ArrayList<>();

        Element root = d.getDocumentElement();
        NodeList cards = root.getElementsByTagName("card");

        // create and store cards
        for (int i = 0; i < cards.getLength(); i++) {

            String cardDescription = "";
            int cardNumber = 0;
            List<Role> onCardRoles = new ArrayList<>();

            // parse card metadata
            Node card = cards.item(i);
            NamedNodeMap cardAttributes = card.getAttributes();

            String name = cardAttributes.getNamedItem("name").getNodeValue();
            String img = cardAttributes.getNamedItem("img").getNodeValue();
            int budget = Integer.parseInt(cardAttributes.getNamedItem("budget").getNodeValue());

            // parse card data
            NodeList cardComponents = card.getChildNodes();
            for (int j = 0; j < cardComponents.getLength(); j++) {

                Node n = cardComponents.item(j);

                // handle parsing scene and part (i.e. role)
                switch (n.getNodeName()) {
                    case "scene":
                        // parse scene metadata
                        cardNumber = Integer.parseInt(n.getAttributes().getNamedItem("number").getNodeValue());

                        // parse, clean scene data
                        cardDescription = n.getTextContent();
                        cardDescription = cardDescription.replaceAll("[ \\n]+", " "); // remove extra spaces, newlines
                        cardDescription = cardDescription.strip(); // remove trailing, leading spaces

                        break;

                    case "part":
                        // parse role metadata
                        String title = n.getAttributes().getNamedItem("name").getNodeValue();
                        int lvl = Integer.parseInt(n.getAttributes().getNamedItem("level").getNodeValue());

                        // parse role data
                        NodeList roleComponents = n.getChildNodes();

                        final int AREA_INDEX = 1;
                        Node area = roleComponents.item(AREA_INDEX);
                        int x = Integer.parseInt(area.getAttributes().getNamedItem("x").getNodeValue());
                        int y = Integer.parseInt(area.getAttributes().getNamedItem("y").getNodeValue());
                        int h = Integer.parseInt(area.getAttributes().getNamedItem("h").getNodeValue());
                        int w = Integer.parseInt(area.getAttributes().getNamedItem("w").getNodeValue());

                        final int LINE_INDEX = 3;
                        Node line = roleComponents.item(LINE_INDEX);
                        String quote = line.getTextContent();

                        onCardRoles.add(new Role(title, lvl, quote, x, y, w, h));

                        break;

                } // parsed card data

            } // parsed card metadata and data

            sceneCards.add(new SceneCard(name, budget, cardDescription, cardNumber, img, onCardRoles));

        } // all cards created

        return sceneCards;

    }

    // reads data as Room objects, returns list of all rooms
    public List<Room> readRooms(Document d) {

        List<Room> rooms = new ArrayList<>();

        Element root = d.getDocumentElement();
        // String board = root.getAttributes().getNamedItem("name").getNodeValue();

        NodeList sets = root.getElementsByTagName("set");

        // create and store rooms
        for (int i = 0; i < sets.getLength(); i++) {

            List<Role> offCardRoles = new ArrayList<>();
            HashSet<String> neighbors = new HashSet<>();
            List<ShotCounter> shotCounters = new ArrayList<>();
            int shotCounter = 0;
            int roomX = 0;
            int roomY = 0;
            int roomH = 0;
            int roomW = 0;

            // parse room metadata
            Node room = sets.item(i);
            NamedNodeMap roomAttributes = room.getAttributes();

            // grabs room name attribute
            String name = roomAttributes.getNamedItem("name").getNodeValue();

            // parse room data
            NodeList roomComponents = room.getChildNodes();
            for (int j = 1; j < roomComponents.getLength(); j += 2) {

                Node n = roomComponents.item(j);

                // handle parsing neighbors, area, takes, and parts
                switch (n.getNodeName()) {
                    case "neighbors":
                        NodeList neighborList = n.getChildNodes();
                        // parse neighbor data
                        for (int k = 1; k < neighborList.getLength(); k += 2) {
                            Node neighbor = neighborList.item(k);
                            String neighborName = neighbor.getAttributes().getNamedItem("name").getNodeValue();
                            neighbors.add(neighborName);
                        }

                        break;

                    case "area":
                        // parse area metadata
                        roomX = Integer.parseInt(n.getAttributes().getNamedItem("x").getNodeValue());
                        roomY = Integer.parseInt(n.getAttributes().getNamedItem("y").getNodeValue());
                        roomH = Integer.parseInt(n.getAttributes().getNamedItem("h").getNodeValue());
                        roomW = Integer.parseInt(n.getAttributes().getNamedItem("w").getNodeValue());

                        break;

                    case "takes":
                        NodeList takeList = n.getChildNodes();
                        // parse take metadata
                        
                        for (int k = 1; k < takeList.getLength(); k += 2) {
                            Node take = takeList.item(k);
                            shotCounter++;
                            // int takeNum = Integer.parseInt(take.getAttributes().getNamedItem("number").getNodeValue());

                            // parse take data
                            NodeList takeComponents = take.getChildNodes();

                            final int AREA_INDEX = 0;
                            Node takeArea = takeComponents.item(AREA_INDEX);
                            int x = Integer.parseInt(takeArea.getAttributes().getNamedItem("x").getNodeValue());
                            int y = Integer.parseInt(takeArea.getAttributes().getNamedItem("y").getNodeValue());
                            int h = Integer.parseInt(takeArea.getAttributes().getNamedItem("h").getNodeValue());
                            int w = Integer.parseInt(takeArea.getAttributes().getNamedItem("w").getNodeValue());

                            // add to shotCounters
                            shotCounters.add(new ShotCounter(x, y, w, h));

                        }

                        break;

                    case "parts":
                        NodeList partList = n.getChildNodes();

                        // parse parts data
                        for (int k = 1; k < partList.getLength(); k += 2) {
                            // parse part metadata
                            Node part = partList.item(k);
                            String title = part.getAttributes().getNamedItem("name").getNodeValue();
                            int lvl = Integer.parseInt(part.getAttributes().getNamedItem("level").getNodeValue());

                            // parse part data
                            NodeList partComponents = part.getChildNodes();

                            final int AREA_INDEX = 1;
                            Node area = partComponents.item(AREA_INDEX);
                            int x = Integer.parseInt(area.getAttributes().getNamedItem("x").getNodeValue());
                            int y = Integer.parseInt(area.getAttributes().getNamedItem("y").getNodeValue());
                            int h = Integer.parseInt(area.getAttributes().getNamedItem("h").getNodeValue());
                            int w = Integer.parseInt(area.getAttributes().getNamedItem("w").getNodeValue());

                            final int LINE_INDEX = 3;
                            Node line = partComponents.item(LINE_INDEX);
                            String quote = line.getTextContent();

                            // add all data to offCardRoles
                            offCardRoles.add(new Role(title, lvl, quote, x, y, w, h));
                        }

                        break;

                }
            }
            // create room
            rooms.add(new SceneRoom(name, shotCounter, neighbors, offCardRoles, shotCounters, roomX, roomY, roomW, roomH));
        }

        // create trailer
        NodeList trailer = root.getElementsByTagName("trailer");
        HashSet<String> trailerNeighbors = new HashSet<>();
        int trailerX = 0;
        int trailerY = 0;
        int trailerH = 0;
        int trailerW = 0;

        Node trailerNode = trailer.item(0);
        NodeList trailerComponents = trailerNode.getChildNodes();
        for (int j = 1; j < trailerComponents.getLength(); j += 2) {
            Node n = trailerComponents.item(j);
            switch (n.getNodeName()) {
                case "neighbors":
                    NodeList neighborList = n.getChildNodes();
                    // parse trailer neighbor data
                    for (int k = 1; k < neighborList.getLength(); k += 2) {
                        Node neighbor = neighborList.item(k);
                        String neighborName = neighbor.getAttributes().getNamedItem("name").getNodeValue();
                        trailerNeighbors.add(neighborName);
                    }

                    break;

                case "area":
                    // parse area metadata
                    trailerX = Integer.parseInt(n.getAttributes().getNamedItem("x").getNodeValue());
                    trailerY = Integer.parseInt(n.getAttributes().getNamedItem("y").getNodeValue());
                    trailerH = Integer.parseInt(n.getAttributes().getNamedItem("h").getNodeValue());
                    trailerW = Integer.parseInt(n.getAttributes().getNamedItem("w").getNodeValue());

                    break;

            }
        }

        rooms.add(new Room("trailer", trailerNeighbors, trailerX, trailerY, trailerW, trailerH));

        // create office
        NodeList office = root.getElementsByTagName("office");
        HashSet<String> officeNeighbors = new HashSet<>();
        int officeX = 0;
        int officeY = 0;
        int officeH = 0;
        int officeW = 0;

        Node officeNode = office.item(0);
        NodeList officeComponents = officeNode.getChildNodes();
        for (int j = 1; j < officeComponents.getLength(); j += 2) {
            Node n = officeComponents.item(j);
            switch (n.getNodeName()) {
                case "neighbors":
                    NodeList neighborList = n.getChildNodes();
                    // parse office neighbor data
                    for (int k = 1; k < neighborList.getLength(); k += 2) {
                        Node neighbor = neighborList.item(k);
                        String neighborName = neighbor.getAttributes().getNamedItem("name").getNodeValue();
                        officeNeighbors.add(neighborName);
                    }

                    break;

                case "area":
                    // parse area metadata
                    officeX = Integer.parseInt(n.getAttributes().getNamedItem("x").getNodeValue());
                    officeY = Integer.parseInt(n.getAttributes().getNamedItem("y").getNodeValue());
                    officeH = Integer.parseInt(n.getAttributes().getNamedItem("h").getNodeValue());
                    officeW = Integer.parseInt(n.getAttributes().getNamedItem("w").getNodeValue());

                    break;

                case "upgrades":
                    NodeList upgradeList = n.getChildNodes();
                    // parse upgrades data
                    for (int k = 1; k < upgradeList.getLength(); k += 2) {
                        // parse upgrade metadata
                        Node upgrade = upgradeList.item(k);
                        int level = Integer.parseInt(upgrade.getAttributes().getNamedItem("level").getNodeValue());
                        String currency = upgrade.getAttributes().getNamedItem("currency").getNodeValue();
                        int amount = Integer.parseInt(upgrade.getAttributes().getNamedItem("amt").getNodeValue());

                        NodeList upgradeComponents = upgrade.getChildNodes();
                        final int AREA_INDEX = 1;
                        Node upgradeArea = upgradeComponents.item(AREA_INDEX);
                        int x = Integer.parseInt(upgradeArea.getAttributes().getNamedItem("x").getNodeValue());
                        int y = Integer.parseInt(upgradeArea.getAttributes().getNamedItem("y").getNodeValue());
                        int h = Integer.parseInt(upgradeArea.getAttributes().getNamedItem("h").getNodeValue());
                        int w = Integer.parseInt(upgradeArea.getAttributes().getNamedItem("w").getNodeValue());

                    }
            }
        }

        rooms.add(new Room("office", officeNeighbors, officeX, officeY, officeW, officeH));

        return rooms;
    }

}
