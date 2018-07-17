package com.wearock.pmppractice;

import android.content.Context;

import com.wearock.pmppractice.controllers.DBHelper;
import com.wearock.pmppractice.models.DBChangeLog;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

public class Application {

    private static Application mApp = null;
    private static Lock locker = new ReentrantLock();

    private int dbVersion;
    private ArrayList<DBChangeLog> lstChangeLogs;

    private Application() {
        lstChangeLogs = new ArrayList<>();
    }

    public static Application getInstance() {
        locker.lock();
        if (mApp == null) {
            mApp = new Application();
        }
        locker.unlock();

        return mApp;
    }

    public void initialize(Context context) throws IllegalStateException {
        try {
            InputSource changeLogs = new InputSource(new StringReader(getChangeLogs(context)));

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(changeLogs);

            XPathFactory xpf = XPathFactory.newInstance();
            XPath xpath = xpf.newXPath();

            // Read database version
            dbVersion = Integer.parseInt(xpath.evaluate("/databaseChangeLog/@version", doc));

            // Read all change sets
            XPathExpression exp = xpath.compile("/databaseChangeLog/changeSet");
            NodeList nodes = (NodeList) exp.evaluate(doc, XPathConstants.NODESET);
            for (int i = 0; i < nodes.getLength(); i++) {
                Node current = nodes.item(i);
                NamedNodeMap attrs = current.getAttributes();

                DBChangeLog log = new DBChangeLog();
                log.setId(attrs.getNamedItem("id").getTextContent());
                log.setAuthor(attrs.getNamedItem("author").getTextContent());
                log.setStatement(current.getTextContent().trim());

                lstChangeLogs.add(log);
            }
        } catch (Exception e) {
            // In case of failed to load database schemas, the application is not in a correct state to start,
            // hence we will need to capture this exception from above, and quite.
            throw new IllegalStateException(context.getResources().getString(R.string.err_changelog_load_failed), e);
        }
    }

    private String getChangeLogs(Context context) throws IOException {
        InputStreamReader reader = new InputStreamReader(context.getResources().getAssets().open("db.changelog.xml"));
        BufferedReader bufReader = new BufferedReader(reader);
        String line;
        String fullLogs = "";
        while ((line = bufReader.readLine()) != null)
            fullLogs += line;
        return fullLogs;
    }

    public DBHelper getDBHelper(Context context) {
        try {
            if (dbVersion != -1) {
                return new DBHelper(context, dbVersion, lstChangeLogs);
            }
        } catch (Exception e) {
            // eat this exception here
        }

        return null;
    }

}
