package com.jinninghui.newspiral.security.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class IoUtil {


    private IoUtil() {
    }

    /**
     * Read all data from an InputStream and returns it as an array.
     * @param in	The stream to read. There is no need to wrap this in a BufferedInputStream.
     * @param piecesize		The size of the blocks to read at a time.
     * @return
     * @throws IOException
     */
    public static byte[] readInputStream(InputStream in, int piecesize) throws IOException {
        ArrayList<byte[]> pieces = new ArrayList<byte[]>();
        while (true) {
            byte[] piece = new byte[piecesize];
            int loc= 0;
            while (loc < piecesize) {
                int read = in.read(piece, loc, piecesize-loc);
                if (read<0) {
                    if (loc!=0 || pieces.size()>1) {
                        byte[] result = new byte[piecesize*pieces.size() + loc];
                        int loc2 = 0;
                        for (byte[] piece2 : pieces) {
                            System.arraycopy(piece2, 0, result, loc2, piecesize);
                            loc2 += piecesize;
                        }
                        System.arraycopy(piece, 0, result, loc2, loc);
                        return result;
                    } else {
                        if (!pieces.isEmpty()) {
                            return pieces.get(0);
                        } else {
                            return new byte[0];
                        }
                    }
                }
                loc += read;
            }
            pieces.add(piece);
        }
    }
}
