package com.tentone.constellations.object;

import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.tentone.constellations.object.worker.WorldWorker;
import com.tentone.constellations.tree.QuadTree;
import com.tentone.constellations.utils.Generator;
import com.tentone.constellations.utils.ThreadUtils;

public class World extends Rectangle
{
	private static final long serialVersionUID = 2597058350349965364L;
	
	//Planets, creatures and players
	public ConcurrentLinkedQueue<Player> players;
	public ConcurrentLinkedQueue<Planet> planets;
	public ConcurrentLinkedQueue<Creature> creatures;
	
	public QuadTree tree;
	
	//Global time
	public double time;
	
	//Workers
	public WorldWorker[] workers;
	
	//Constructor
	public World(int width, int height)
	{
		super(0, 0, width, height);
		
		this.players = new ConcurrentLinkedQueue<Player>();
		this.planets = new ConcurrentLinkedQueue<Planet>();
		this.creatures = new ConcurrentLinkedQueue<Creature>();
		this.tree = new QuadTree(x, y, width, height);
		
		this.time = 0.0;
		
		//Create 4 workers
		this.workers = new WorldWorker[4];
		for(int i = 0; i < 4; i++)
		{
			this.workers[i] = new WorldWorker(null, 0);
		}
	}
	
	//Generate random world with 2 players
	public static World generateWorld(int width, int height)
	{
		World world = new World(width, height);
		int players = 5;
		
		for(int i = 0; i < players; i++)
		{
			Player player = new Player("User" + Generator.generateID());
			world.addPlayer(player);
			
			Planet planet = new Planet(3);
			while(!world.contains(planet))
			{
				planet.setPosition(MathUtils.random(world.width * 1.0f / players * i, world.width * 1.0f / players * (i + 1)), MathUtils.random(world.height));
			}
			
			planet.setOwner(player);
			planet.setLevel(1);
			world.addPlanet(planet);
		}
		
		//Create planets
		for(int i = 0; i < 20; i++)
		{
			boolean colliding = true;
			int attempt = 0;
			
			Planet planet = null;
			
			while(colliding)
			{
				if(attempt > 100)
				{
					i = 0;
					while(world.planets.peek().owner == null)
					{
						world.planets.poll();
					}
				}
				
				colliding = false;
				
				planet = new Planet(MathUtils.random(1, 2));
				while(!world.contains(planet))
				{					
					planet.setPosition(MathUtils.random(world.width), MathUtils.random(world.height));
				}
				
				Iterator<Planet> itp = world.planets.iterator();
				while(itp.hasNext())
				{
					if(planet.overlaps(itp.next()))
					{
						colliding = true;
						break;
					}
				}
				
				attempt++;
			}
			
			world.addPlanet(planet);
		}
		
		return world;
	}
	
	//Update world state and render
	public void render(float delta, ShapeRenderer shape)
	{
		//Don't let delta get to high or too low (10~100fps)
		if(delta > 0.1f)
		{
			delta = 0.1f;
		}
		else if(delta < 0.01f)
		{
			delta = 0.01f;
		}
		
		//Update world time
		this.time += delta;
		
		Thread[] thread;
		
		//Start world update threads
		if(this.tree.isLeaf())
		{
			thread = new Thread[1];
			
			this.workers[0].set(this.tree, delta);
			thread[0] = new Thread(this.workers[0]);
			thread[0].run();
		}
		else
		{
			thread = new Thread[4];
			
			for(int i = 0; i < 4; i++)
			{
				this.workers[i].set(this.tree.children[i], delta);
				thread[i] = new Thread(this.workers[i]);
				thread[i].run();
			}
		}

		//Update and draw planets
		Iterator<Planet> itp = this.planets.iterator();
		while(itp.hasNext())
		{
			Planet planet = itp.next();
			
			//Update planet
			planet.update(delta);
			
			//Draw planet
			shape.setColor((planet.owner == null) ? Color.GRAY : planet.owner.color);
			
			shape.set(ShapeType.Filled);
			shape.circle(planet.x, planet.y, planet.life / (float) Planet.life_per_level, 32);
			shape.rect(planet.x - 0.75f, planet.y - planet.level - 1f, 0.015f * (planet.life % Planet.life_per_level), 0.2f);
			
			shape.set(ShapeType.Line);
			for(int i = planet.level; i <= planet.size; i++)
			{
				shape.circle(planet.x, planet.y, i, 32);
			}
			shape.rect(planet.x - 0.75f, planet.y - planet.level - 1f, 1.5f, 0.2f);
		}
				
		shape.set(ShapeType.Filled);

		//Wait for all threads to finish updating
		while(!ThreadUtils.allFinished(thread)){};
		
		//Draw creatures
		Iterator<Creature> itc = this.creatures.iterator();
		while(itc.hasNext())
		{
			Creature creature = itc.next();
			shape.setColor(creature.owner.color);
			shape.circle(creature.x, creature.y, 0.1f, 4);
		}
	}
	
	//Add a creature to the world
	public void addPlayer(Player player)
	{
		this.players.add(player);
	}
	
	//Add a planet to the world
	public void addPlanet(Planet planet)
	{
		planet.world = this;
		
		this.planets.add(planet);
	}
	
	//Add a creature to the world
	public void addCreature(Creature creature)
	{
		creature.world = this;
		this.creatures.add(creature);
		this.tree.add(creature);
	}
	
	//Check if a planet is inside the world
	public boolean contains(Circle circle)
	{
		return circle.x - circle.radius >= x && circle.x + circle.radius <= x + width
				&& circle.y - circle.radius >= y && circle.y + circle.radius <= y + height;
	}
}
