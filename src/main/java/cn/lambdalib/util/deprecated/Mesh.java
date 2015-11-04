package cn.lambdalib.util.deprecated;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.Tessellator;

import org.lwjgl.opengl.GL11;

/**
 * @author WeAthFolD
 */
@Deprecated // Too bad abstraction and efficiency. Work harder!
public class Mesh {

	protected double[][] vertices;
	
	protected double[][] uvs;
	
	protected float[][] normals;
	
	protected int[] triangles;
	
	protected List<Mesh> sub = new ArrayList<Mesh>();
	
	/**
	 * Determine whether this mesh needs to be buffered(Compiled into a display list).
	 */
	protected boolean doesBuffer = false;
	
	int listID = -1;
	
	public Mesh() {}
	
	public int addMesh(Mesh m) {
		sub.add(m);
		return sub.size() - 1;
	}
	
	public void removeMesh(Mesh m) {
		sub.remove(m);
	}
	
	public Mesh getSubMesh(int id) {
		return sub.get(id);
	}
	
	public Mesh setUVs(double[][] uvs) {
		if(vertices == null || vertices.length != uvs.length) {
			throw new IllegalStateException("UVs size must be equal to vert size");
		}
		this.uvs = uvs;
		return this;
	}
	
	public Mesh setVertices(double[][] vertices) {
		this.vertices = vertices;
		if(uvs != null && uvs.length != vertices.length) uvs = null;
		if(normals != null && normals.length != vertices.length) normals = null;
		//Dont check triangle.
		return this;
	}
	
	public Mesh setVertex(int ind, double[] vert) {
		vertices[ind] = vert;
		return this;
	}
	
	public Mesh setUV(int ind, double[] uv) {
		uvs[ind] = uv;
		return this;
	}
	
	public Mesh setTriangles(int[] triangles) {
		this.triangles = triangles;
		return this;
	}
	
	public Mesh setAllNormals(float[] normal) {
		normals = new float[vertices.length][];
		for(int i = 0; i < vertices.length; ++i) {
			normals[i] = normal;
		}
		return this;
	}
	
	public Mesh setNormals(float[][] normals) {
		if(vertices == null || vertices.length != uvs.length) {
			throw new IllegalStateException("Normals size must be equal to vert size");
		}
		this.normals = normals;
		return this;
	}
	
	/**
	 * Decompose to triangles.
	 */
	public Mesh setQuads(int[] quads) {
		if(quads.length % 4 != 0) {
			System.err.println("You should specify quads by a list of length of multiply of 4.");
		}
		int[] result = new int[(quads.length / 4) * 6];
		int j = 0;
		for(int i = 0; i + 3 < quads.length; i += 4, j += 6) {
			result[j] 	  = quads[i];
			result[j + 1] = quads[i + 1];
			result[j + 2] = quads[i + 2];
			
			result[j + 3] = quads[i];
			result[j + 4] = quads[i + 2];
			result[j + 5] = quads[i + 3];
		}
		setTriangles(result);
		return this;
	}
	
	public Mesh setQuads(Integer[] quads) {
		if(quads.length % 4 != 0) {
			System.err.println("You should specify quads by a list of length of multiply of 4.");
		}
		int[] result = new int[(quads.length / 4) * 6];
		int j = 0;
		for(int i = 0; i + 3 < quads.length; i += 4, j += 6) {
			result[j] 	  = quads[i];
			result[j + 1] = quads[i + 1];
			result[j + 2] = quads[i + 2];
			
			result[j + 3] = quads[i];
			result[j + 4] = quads[i + 2];
			result[j + 5] = quads[i + 3];
		}
		setTriangles(result);
		return this;
	}
	
	private void redraw(Material mat, boolean execute) {
		if(doesBuffer) {
			if(listID == -1) {
				listID = GL11.glGenLists(1);
				GL11.glNewList(listID, execute ? GL11.GL_COMPILE_AND_EXECUTE : GL11.GL_COMPILE);
			}
		}
		
		mat.onRenderStage(RenderStage.START);
		GL11.glPushMatrix();
		
		mat.onRenderStage(RenderStage.BEFORE_TESSELLATE);
		Tessellator t = Tessellator.instance;
		t.startDrawing(GL11.GL_TRIANGLES);
		mat.onRenderStage(RenderStage.START_TESSELLATE);
		
		if(uvs != null) {
			if(triangles != null) {
				for(int i : triangles) {
					double[] vert = vertices[i];
					double[] uv = uvs[i];
					if(normals != null) {
						t.setNormal(normals[i][0], normals[i][1], normals[i][2]);
					}
					t.addVertexWithUV(vert[0], vert[1], vert[2], uv[0], uv[1]);
					
				}
			}
		} else {
			if(triangles != null) {
				for(int i : triangles) {
					double[] vert = vertices[i];
					if(normals != null) {
						t.setNormal(normals[i][0], normals[i][1], normals[i][2]);
					}
					t.addVertex(vert[0], vert[1], vert[2]);
				}
			}
		}
		t.draw();
		
		GL11.glPopMatrix();
		mat.onRenderStage(RenderStage.END);
		
		for(Mesh m : this.sub) {
			m.draw(mat);
		}
		
		if(doesBuffer) {
			GL11.glEndList();
		}
	}
	
	public void redraw(Material mat) {
		redraw(mat, true);
	}
	
	public void draw(Material mat) {
		if(doesBuffer) {
			if(listID == -1) {
				redraw(mat, true);
			} else
				GL11.glCallList(listID);
		} else {
			redraw(mat, true);
		}
	}
	
	
}
