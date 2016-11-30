package com.tentone.constellations.camera;

//Orthographic with aspect ratio and defined size
public class OrthographicCamera extends com.badlogic.gdx.graphics.OrthographicCamera
{
	//Size and aspect ratio
	public float aspect_ratio;
	public float size;
	public float size_ratio;
	
	//Resize mode
	public ResizeMode resize_mode;
	
	//Constructor
	public OrthographicCamera(float size, float aspect_ratio, float screen_width)
	{
		super(size * aspect_ratio, size);
		
		this.aspect_ratio = aspect_ratio;
		this.size = size;
		this.size_ratio = size * aspect_ratio / screen_width;
				
		this.resize_mode = ResizeMode.HORIZONTAL;
	}
	
	//Set camera aspect ratio
	public void setAspectRatio(float aspect_ratio)
	{
		this.aspect_ratio = aspect_ratio;
	}
	
	//Set camera size
	public void setSize(float size)
	{
		this.size = size;
	}
	
	//Set camera resize mode
	public void setResizeMode(ResizeMode mode)
	{
		this.resize_mode = mode;
	}
	
	//Update size ratio
	public void updateSizeRatio(int width)
	{
		if(this.resize_mode == ResizeMode.HORIZONTAL)
		{
			this.size_ratio = this.size * this.aspect_ratio / (float)width;
		}
		else if(this.resize_mode == ResizeMode.VERTICAL)
		{
			this.size_ratio = this.size / (float)width;
		}
	}
	
	//Update camera (call after change any attribute)
	@Override
	public void update()
	{
		if(this.resize_mode == ResizeMode.HORIZONTAL)
		{
			this.viewportWidth = this.size * this.aspect_ratio;
			this.viewportHeight = this.size;
		}
		else if(this.resize_mode == ResizeMode.VERTICAL)
		{
			this.viewportWidth = this.size;
			this.viewportHeight = this.size / this.aspect_ratio;
		}
		
		super.update();
	}
}

