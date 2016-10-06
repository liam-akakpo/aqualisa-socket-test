package dk.nodes.netscanner;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by liamakakpo on 21/09/2016.
 */
public class NetworkScanner {

    private static ArrayList<NetworkDevice> devices = new ArrayList<>();

    private static final int TIMEOUT = 300;

    private static ExecutorService executorService = Executors.newCachedThreadPool();

    private static void scanHosts(final String base, final int start, final int length) {
        executorService.execute(() -> {
            for (int i = start; i < length; i++) {
                String target = base + "." + i;
                try {
                    InetAddress inetAddress = InetAddress.getByName(target);
                    if (inetAddress.isReachable(TIMEOUT)) {
                        System.out.printf("[%s]: " + inetAddress.getHostName() + "\n", target);
                    }
                    synchronized (devices) {
                        devices.add(new NetworkDevice(target, inetAddress.getHostName()));
                    }
                } catch (IOException e) {

                }
            }
        });
    }

    private static void print(final String message) {
        System.out.println(message);
    }

    private static String localHost;

    private static final int THREAD_COUNT = 5;

    private static void connectTo(final String host, final int port) {
        try {
            final DatagramSocket socket = new DatagramSocket();

            System.out.print("\nEstablishing UDP connection to " + host + " on port " + port);

            final Scanner in = new Scanner(System.in);
            System.out.print("\nEnter data to send: ");
            final String input = in.nextLine();

            final byte[] bytes = input.getBytes();

            final InetAddress address = InetAddress.getByName(host);
            final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);

            //socket.setSoTimeout(15000);
            socket.send(packet);

            System.out.println("\nMessasge sent. Listening on port " + socket.getPort());

            final DatagramPacket response = new DatagramPacket(bytes, bytes.length);
            socket.receive(response);
            String received = new String(response.getData(), 0, response.getLength());
            System.out.println("Response: " + received);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("\nFailed to send packet!");
            e.printStackTrace();
        }
    }

    private static void startServer(final int port) {
        try {
            final DatagramSocket socket = new DatagramSocket(port);
            while (true) {
                byte[] buf = new byte[8];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                socket.receive(packet);

                InetAddress address = packet.getAddress();

                int clientPort = packet.getPort();
                System.out.println("Received: " + new String(buf));
                System.out.println("From: " + address.getHostName() + "(" + address.getAddress() + ") from port: " + clientPort);

                final Scanner in = new Scanner(System.in);
                System.out.print("\nResponse: ");
                final String input = in.nextLine();

                final byte[] bytes = input.getBytes();

                packet = new DatagramPacket(bytes, bytes.length, address, clientPort);
                socket.send(packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final boolean SCAN_ENABLED = false;
    private static final boolean SERVER = true;

    private static final boolean PORT_SCAN = false;

    private static void scanHost(final String host, final int start, final int end) {
        for (int i = start; i < end; i++) {
            try {
                Socket clientSocket = new Socket();
                clientSocket.setSoTimeout(300);
                clientSocket.connect(new InetSocketAddress(host, i));
                System.out.println("Port " + i + " OPEN");
            } catch (IOException e) {
                System.out.println("Port " + i + " ...");
            }
        }
    }

    public static final void main(String[] args) {
        if (SCAN_ENABLED) {
            try {
                localHost = Inet4Address.getLocalHost().getHostAddress();
                print("This machine: " + localHost);
                final String base = localHost.substring(0, localHost.lastIndexOf("."));
                print("Base: " + base);
                final String base2 = base.substring(0, base.lastIndexOf("."));
                String subnet;

                for (int i = 15; i < 16; i++) {
                    subnet = new String(base2) + "." + i;
                    int division = 255 / THREAD_COUNT;
                    for (int j = 0; j < THREAD_COUNT; j++) {
                        print("Thread [" + j + "]: " + (j * division) + " - " + ((j + 1) * division));
                        scanHosts(subnet, j * division, ((j + 1) * division));
                    }
                }
                executorService.shutdown();
                boolean finshed = executorService.awaitTermination(1, TimeUnit.MINUTES);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (SERVER) {
            final Scanner in = new Scanner(System.in);
            System.out.print("\nEnter a port to listen on: ");
            final String input = in.nextLine();
            int value = Integer.parseInt(input);
            startServer(value);

        } else if (PORT_SCAN) {
            final Scanner in = new Scanner(System.in);
            System.out.print("\nEnter a host to scan ports [ip-address]: ");
            String input = in.nextLine();
            scanHost(input, 20, Integer.MAX_VALUE);

        } else {
            final Scanner in = new Scanner(System.in);
            System.out.print("\nEnter a host to connect to [ip-address:port]: ");
            String input = in.nextLine();

            String[] host = input.split(":");
            connectTo(host[0], Integer.parseInt(host[1]));
        }
    }
}
