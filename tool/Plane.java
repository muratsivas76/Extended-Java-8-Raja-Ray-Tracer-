/* $Id: Plane.java,v 1.1.1.1 2001/01/08 23:10:14 gregoire Exp $
 * Copyright (C) 1999-2000 E. Fleury & G. Sutre
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package raja.shape;

import raja.*;
import raja.io.*;
import java.util.HashMap;


public class Plane extends BasicForm implements java.io.Serializable, Writable
{
    protected Point3D origin;
    protected Vector3D normal;

    public Plane(Point3D origin, Vector3D normal)
    {
        this.origin = origin;
        this.normal = Vector3D.normalization(normal);
    }

    public static Object build(ObjectReader reader)
	throws java.io.IOException
    {
	/* Initialisation */
	HashMap map = new HashMap();

	map.put("origin",null);
	map.put("normal",null);

	/* Parsing */
	reader.readFields(map);

	return new Plane((Point3D) map.get("origin"),
                         (Vector3D) map.get("normal"));
    }

    public Point3D computeIntersection(Ray r)
    {
        double den = Vector3D.dotProduct(normal, r.direction);

        if (den == 0)
        {
            return null;
        }
        else
        {
            double num = Vector3D.dotProduct(normal, new Vector3D(r.origin, origin));
            double t = num / den;
            if (t <= 0)
            {
                return null;
            }
            if (hasLG(r.origin)) {
                return null;
            }
            return new Point3D(r.origin, Vector3D.product(r.direction, t));
        }
    }
    public boolean exactlyContains(Point3D p)
    {
        return (Vector3D.dotProduct(normal, new Vector3D(origin, p)) <= 0);
    }
    public boolean exactlyStrictlyContains(Point3D p)
    {
        return (Vector3D.dotProduct(normal, new Vector3D(origin, p)) < 0);
    }
    public Vector3D computeNormal(Point3D p)
    {
        return normal;   // Attention a la normale qui doit etre extérieure !
    }

    public String toString()
    {
        return ObjectWriter.toString(this);
    }
    public void write(ObjectWriter writer) throws java.io.IOException
    {
        Object[][] fields = { { "origin", origin },
                              { "normal", normal } };
        writer.writeFields(fields);
    }
}
