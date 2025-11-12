package raja.test;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.JProgressBar;
import javax.swing.event.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.event.*;
import java.io.*;
import com.sun.media.jai.codec.*;

import raja.renderer.*;
import raja.io.*;


class ComputeVideo
{
    public static void main(String[] argv) throws java.io.IOException, java.lang.ClassNotFoundException
    {
        if (argv.length <= 1)
        {
            System.out.println("usage : java raja.test.ComputeVideo [-noprogress -scale scale -exact -antialias level -n first-last] depth sceneFile travellingFile videoBaseName");
            return;
        }

        boolean noProgressBar = false;
        boolean exact = false;
        float scale = 1;
        int antialias = 0;
        int first = 1;
        int last = 0;
        int index;

        for(index = 0 ; ; index++)
        {
            if (argv[index].equals("-noprogress")) {
                noProgressBar = true;
            }
            else if (argv[index].equals("-scale")) {
                scale = Float.parseFloat(argv[++index]);
            }
            else if (argv[index].equals("-antialias")) {
                antialias = Integer.parseInt(argv[++index]);
            }
            else if (argv[index].equals("-exact")) {
                exact = true;
            }
            else if (argv[index].equals("-n")) {
                String region = argv[++index];
                int i = region.indexOf('-');
                first = Integer.parseInt(region.substring(0, i));
                last  = Integer.parseInt(region.substring(i+1));
                if (first > last) {
                    throw new IllegalArgumentException("first > last");
                }
                if ((first <= 0)) {
                    throw new IllegalArgumentException("first <= 0");
                }
                if ((last <= 0)) {
                    throw new IllegalArgumentException("last <= 0");
                }
            }
            else {
                break;
            }
        }

        int depth = Integer.parseInt(argv[index++]);

        Reader in = new InputStreamReader(new FileInputStream(argv[index]));
        ObjectReader reader = new ObjectReader(in);
        Scene scene = (Scene) reader.readObject();
        reader.close();
        index++;

        InputStream inputStream = new BufferedInputStream(new FileInputStream(argv[index]));
        ObjectInput objectInput = new ObjectInputStream(inputStream);
        Camera[] travelling = (Camera[]) objectInput.readObject();
        objectInput.close();
        index++;

        if (first > travelling.length) {
            throw new IllegalArgumentException("first > travelling.length");
        }

        if (last == 0) {
            last = travelling.length;
        }
        else if (last > travelling.length) {
            throw new IllegalArgumentException("last > travelling.length");
        }

        Resolution resolution = new Resolution((int) Math.round(384 * scale), (int) Math.round(288 * scale));
        RayTracer rayTracer;

        if (exact) {
            rayTracer = new AdvancedRayTracer(scene.getWorld(), depth, 0);
        }
        else {
            rayTracer = new AdvancedRayTracer(scene.getWorld(), depth);
        }

        Sampler sampler;

        if (antialias > 0) {
            sampler = new NaiveSuperSampler(antialias);
        }
        else {
            sampler = new BasicSampler();
        }

        JFrame progressFrame = null;

        if (! noProgressBar) {
            progressFrame = new JFrame("Raja");
            progressFrame.getContentPane().setLayout(new BorderLayout(10, 10));
            progressFrame.setBounds(300, 300, 300, 120);
            progressFrame.show();
        }

        for(int i = first ; i <= last ; i++)
        {
            Camera camera = travelling[i-1];

            System.out.println("\nCalcul de l'image " + i + "/" + travelling.length);

            Renderer renderer = new BasicRenderer(camera, resolution, rayTracer, sampler, BufferedImage.TYPE_3BYTE_BGR);
            BufferedImage image = renderer.getImage();
            BoundedRangeModel model = renderer.getModel();

            long starttime, endtime;
            JProgressBar progress = null;
            JLabel label = null;

            if (! noProgressBar) {
                progress = new JProgressBar();
                progress.setPreferredSize(new Dimension(100, 40));
                Border border = progress.getBorder();
                Border margin = new EmptyBorder(0, 10, 10, 10);
                progress.setBorder(new CompoundBorder(margin, border));
                progress.setStringPainted(true);

                label = new JLabel("Calcul de l'image " + i + "/" + travelling.length, SwingConstants.CENTER);
                label.setForeground(Color.black);

                progressFrame.getContentPane().add(label, BorderLayout.CENTER);
                progressFrame.getContentPane().add(progress, BorderLayout.SOUTH);
                progressFrame.validate();
                progress.setModel(model);
            }

            starttime = System.currentTimeMillis();
            renderer.run();
            endtime = System.currentTimeMillis();

            if (! noProgressBar) {
                progressFrame.getContentPane().remove(label);
                progressFrame.getContentPane().remove(progress);
            }

            System.out.println("Temps de calcul : " + ((double) endtime - starttime) / 1000 + " secondes");

            String fileName = argv[index] + "-" + i + ".pnm";
            System.out.print("Enregistrement du fichier PNM " + fileName + " : ");
            OutputStream os = new BufferedOutputStream(new FileOutputStream(fileName));
            ImageEncoder enc = ImageCodec.createImageEncoder("PNM", os, new PNMEncodeParam());
            enc.encode(image);
            os.close();
            System.out.println("terminé.\n");
        }

        if (! noProgressBar) {
            progressFrame.setVisible(false);
            progressFrame.dispose();
            System.exit(0);
        }
    }
}
