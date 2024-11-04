package org.cduggan;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class DPI {
    public static void inspectSNI(InputStream clientInput, OutputStream clientOutput) throws IOException {
        byte[] buffer = new byte[5];
        clientInput.read(buffer);

        if (buffer[0] == 0x16 && buffer[1] == 0x03 && buffer[5] == 0x01) {
            byte[] clientHello = new byte[buffer[3] << 8 | buffer[4]];
            clientInput.read(clientHello);

            int pos = 0;
            while (pos < clientHello.length - 4) {
                if (clientHello[pos] == 0x00 && clientHello[pos + 1] == 0x00) {
                    int nameType = clientHello[pos + 2];
                    int nameLen = clientHello[pos + 3] << 8 | clientHello[pos + 4];
                    if (nameType == 0x00) {
                        String serverName = new String(clientHello, pos + 5, nameLen);
                        System.out.println("HTTPS request to SNI: " + serverName);
                        return;
                    }
                }
                pos++;
            }
        }
    }
}
