package raja.test;

import raja.*;
import raja.renderer.*;

import java.io.*;


class CreateMickeyTravelling
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

            double x = 70*Math.cos(angle);
            double y = 70*Math.sin(angle);

            travelling[i] = new HorizontalCamera(new Point3D(x + 70, y, 35), new Vector3D(-x, -y, -35), 1.8, 2, 1.5);
        }

        OutputStream output = new BufferedOutputStream(new FileOutputStream(argv[0]));
        ObjectOutput objectOutput = new ObjectOutputStream(output);
        objectOutput.writeObject(travelling);
        objectOutput.flush();
        objectOutput.close();
    }
}
