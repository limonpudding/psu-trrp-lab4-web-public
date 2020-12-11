package trrp.lab4.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import trrp.lab4.dto.EgrulInfoDTO;
import trrp.lab4.dto.EgrulUsersDTO;
import trrp.lab4.util.WorkerAction;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;

@Component
public class EgrulWorkerConnector implements Runnable {

    private DatagramSocket socket;
    private HashMap<Integer, ConnectionHolder> workersMap = new HashMap<>();

    private Integer pdfWorkerConnectorPort;

    public EgrulWorkerConnector(@Value("${pdf.worker.connector.port}") Integer pdfWorkerConnectorPort) {
        this.pdfWorkerConnectorPort = pdfWorkerConnectorPort;
    }

    @Override
    public void run() {
        byte[] recvBuf;
        DatagramPacket packet;

        try {
            socket = new DatagramSocket(pdfWorkerConnectorPort);
            while (true) {
                recvBuf = new byte[6400];
                packet = new DatagramPacket(recvBuf, recvBuf.length);
                socket.receive(packet);
                String reseivedString = new String(recvBuf, StandardCharsets.UTF_8).trim();
                String[] receivedParams = reseivedString.split(";");

                WorkerAction action = WorkerAction.getByName(receivedParams[0]);
                if (action == null) {
                    continue;
                }
                if (action == WorkerAction.NEW_WORKER) {
                    Socket workerSocket = new Socket(receivedParams[1], Integer.parseInt(receivedParams[2]));
                    BufferedOutputStream os = new BufferedOutputStream(workerSocket.getOutputStream());
                    byte[] message = WorkerAction.CREATE_CONNECTION.name().getBytes();
                    os.write(message);
                    os.flush();
                    ConnectionHolder connectionHolder = new ConnectionHolder(workerSocket, Boolean.FALSE);
                    workersMap.put(getNextIndex(), connectionHolder);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Integer getNextIndex() {
        Integer max = 1;
        for (Entry entry:workersMap.entrySet()) {
            if ((Integer)entry.getKey() > max) {
                max = ( Integer) entry.getKey();
            }
        }
        return max;
    }

    public EgrulUsersDTO getEgrulDataFromWorker(String ogrn) throws IOException {
        ConnectionHolder connectionHolder = null;
        while (connectionHolder == null) {
            for (Entry entry : workersMap.entrySet()) {
                if (((ConnectionHolder) entry.getValue()).getBusy() == Boolean.FALSE) {
                    connectionHolder = (ConnectionHolder) entry.getValue();
                    connectionHolder.setBusy(Boolean.TRUE);
                }
            }
        }
        Socket workerSocket = connectionHolder.getSocket();
        BufferedOutputStream os = new BufferedOutputStream(workerSocket.getOutputStream());
        BufferedInputStream is = new BufferedInputStream(workerSocket.getInputStream());
        byte[] message = ogrn.getBytes();
        os.write(message);
        os.flush();
        byte[] recvBuf = new byte[6400];
        is.read(recvBuf);
        String jsonString = new String(recvBuf, StandardCharsets.UTF_8).trim();
        connectionHolder.setBusy(Boolean.FALSE);
        if (jsonString.equals("")) {
            return null;
        } else {
            return createDtoFromJson(jsonString);
        }
    }

    private EgrulUsersDTO createDtoFromJson(String jsonString) {
        JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();
        EgrulUsersDTO egrulUser = new EgrulUsersDTO();
        egrulUser.setFirstName(jsonObject.getAsJsonPrimitive("firstName").getAsString());
        egrulUser.setLastName(jsonObject.getAsJsonPrimitive("lastName").getAsString());
        egrulUser.setPatronymic(jsonObject.getAsJsonPrimitive("patronymic").getAsString());
        egrulUser.setOgrn(jsonObject.getAsJsonPrimitive("ogrn").getAsString());

        List<EgrulInfoDTO> egrulInfos = new ArrayList<>();
        JsonArray jsonArray = jsonObject.getAsJsonArray("activities");
        for (int i = 0; i < jsonArray.size(); i++) {
            JsonObject activity = jsonArray.get(i).getAsJsonObject();
            EgrulInfoDTO info = new EgrulInfoDTO();
            info.setActivityCode(activity.getAsJsonPrimitive("activityCode").getAsString());
            info.setDescription(activity.getAsJsonPrimitive("description").getAsString());
            info.setIsMain(activity.getAsJsonPrimitive("isMain").getAsBoolean());
            egrulInfos.add(info);
        }
        egrulUser.setEgrulInfoDTOS(egrulInfos);
        return egrulUser;
    }
}
