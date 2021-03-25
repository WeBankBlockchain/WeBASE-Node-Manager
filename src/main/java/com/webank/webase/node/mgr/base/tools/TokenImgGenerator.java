/**
 * Copyright 2014-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webank.webase.node.mgr.base.tools;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.security.SecureRandom;
import java.util.Base64;
import javax.imageio.ImageIO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 图形验证码生成
 */
public class TokenImgGenerator {

    private static Logger log = LoggerFactory.getLogger(TokenImgGenerator.class);

    private static SecureRandom generator = new SecureRandom();

    public TokenImgGenerator() {

    }

    public static Color getRandColor(int i, int j) {
        SecureRandom random = new SecureRandom();
        if (i > 255) {
            i = 255;
        }
        if (j > 255) {
            j = 255;
        }
        int k = i + random.nextInt(j - i);
        int l = i + random.nextInt(j - i);
        int i1 = i + random.nextInt(j - i);
        return new Color(k, l, i1);
    }

    private static void shear(Graphics g, int w1, int h1, Color color) {
        shearX(g, w1, h1, color);
        shearY(g, w1, h1, color);
    }

    public static void shearX(Graphics g, int w1, int h1, Color color) {
//		int period = generator.nextInt(2);
        int period = generator.nextInt(200) + 10;

        int frames = 1;
//		int phase = generator.nextInt(2);
        int phase = generator.nextInt(200);

        for (int i = 0; i < h1; i++) {
            double d = (double) (period >> 1)
                * Math.sin((double) i / (double) period
                + (6.2831853071795862D * (double) phase)
                / (double) frames);
            g.copyArea(0, i, w1, 1, (int) d, 0);
        }

    }

    public static void shearY(Graphics g, int w1, int h1, Color color) {

        int period = generator.nextInt(5) + 2; // 50;

        boolean borderGap = true;
        int frames = 20;
        int phase = 7;
        for (int i = 0; i < w1; i++) {
            double d = (double) (period >> 1)
                * Math.sin((double) i / (double) period
                + (6.2831853071795862D * (double) phase)
                / (double) frames);
            g.copyArea(i, 0, 1, h1, 0, (int) d);
            if (borderGap) {
                g.setColor(color);
                g.drawLine(i, (int) d, i, 0);
                g.drawLine(i, (int) d + h1, i, h1);
            }
        }
    }


    /**
     * get buffer image.
     */
    private static BufferedImage getBufferedImage(String s) {
        int i = 155;
        byte byte0 = 60;
        BufferedImage bufferedimage = new BufferedImage(i, byte0, 1);
        Graphics g = bufferedimage.getGraphics();
        SecureRandom random = new SecureRandom();
        g.setColor(new Color(255, 255, 255));
        g.fillRect(0, 0, i, byte0);
        g.setFont(new Font("Arial", Font.BOLD, 36));
        g.setColor(getRandColor(160, 200));
        for (int j = 0; j < 10; j++) {
            int l = random.nextInt(i);
            int i1 = random.nextInt(byte0);
            int j1 = random.nextInt(12);
            int k1 = random.nextInt(12);
            g.drawOval(l, i1, l + j1, i1 + k1);
        }

        for (int k = 0; k < s.length(); k++) {
            char c = s.charAt(k);
            String s1 = String.valueOf(c);
            g.setColor(new Color(20 + random.nextInt(110), 20 + random
                .nextInt(110), 20 + random.nextInt(110)));
            g.drawString(s1, ((i - 36) / s.length()) * k + 18, 42);
        }
        shear(g, bufferedimage.getWidth(), bufferedimage.getHeight(),
            new Color(240, 248, 255));

        g.dispose();

        return bufferedimage;
    }


    /**
     * response pitcher.
     */
    public static void createPic(OutputStream outputstream, String msg)
        throws IOException {
        BufferedImage bufferedimage = getBufferedImage(msg);
        ImageIO.write(bufferedimage, "JPEG", outputstream);

    }


    /**
     * response base64.
     */
    public static String getBase64Image(String msg)
        throws IOException {
        BufferedImage bufferedimage = getBufferedImage(msg);
        ByteArrayOutputStream bs = null;
        try {
            bs = new ByteArrayOutputStream();
            ImageIO.write(bufferedimage, "png", bs);//将绘制得图片输出到流
            return Base64.getEncoder().encodeToString(bs.toByteArray());
        } catch (Exception e) {
            log.error("fail createPic.", e);
            return null;
        } finally {
            if (bs != null) {
                bs.close();
                bs.flush();
            }
        }
    }

}
