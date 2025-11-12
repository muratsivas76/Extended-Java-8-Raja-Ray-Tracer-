package raja.test;

import raja.*;
import raja.renderer.*;
import java.io.*;


class CreateAdvancedCristalTravelling
{
    public static void main(String[] argv) throws java.io.IOException
    {
        if (argv.length == 0)
        {
            System.out.println("Vous devez passer un nom de fichier en argument");
            return;
        }

        int N = 100;

        Camera[] travelling = new Camera[N];

        for(int i = 0 ; i < N ; i++)
        {
            double angle = (2 * Math.PI * i) / N;
            angle +=  Math.PI;

            double x = 140*Math.cos(angle);
            double y = 140*Math.sin(angle);

            travelling[i] = new HorizontalCamera(new Point3D(x + 140, y, 130), new Vector3D(-x, -y, -130), 1.8, 2, 1.5);
        }

        OutputStream output = new BufferedOutputStream(new FileOutputStream(argv[0]));
        ObjectOutput objectOutput = new ObjectOutputStream(output);
        objectOutput.writeObject(travelling);
        objectOutput.flush();
        objectOutput.close();
    }
}
