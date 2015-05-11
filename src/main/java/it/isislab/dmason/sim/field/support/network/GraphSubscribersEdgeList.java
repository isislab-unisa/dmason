/**
 * Copyright 2012 Universita' degli Studi di Salerno


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package it.isislab.dmason.sim.field.support.network;

import it.isislab.dmason.annotation.AuthorAnnotation;

import java.util.ArrayList;
import java.util.HashMap;
@AuthorAnnotation(
		author = {"Ada Mancuso","Francesco Milone","Carmine Spagnuolo"},
		date = "6/3/2014"
		)
/**
 * 
 * @author Ada Mancuso
 * @author Francesco Milone
 * @author Carmine Spagnuolo
 * 
 * this class contains the Super-Graph edges utilized for create/subscribe Topic
 * 
 */
public class GraphSubscribersEdgeList {

	private HashMap<Integer, ArrayList<Integer>> toSubscribeList;
	private HashMap<Integer, ArrayList<Integer>> toPublishList;


	public GraphSubscribersEdgeList() {
		toSubscribeList = new HashMap<Integer, ArrayList<Integer>>();
		toPublishList = new HashMap<Integer, ArrayList<Integer>>();
	}

	public void addEdge(int from, int to, boolean isDirected)
	{
		if(toSubscribeList.get(from)==null)
		{
			toSubscribeList.put(from, new ArrayList<Integer>());
			toPublishList.put(from, new ArrayList<Integer>());
		}
		if(toSubscribeList.get(to)==null)
		{
			toSubscribeList.put(to, new ArrayList<Integer>());
			toPublishList.put(to, new ArrayList<Integer>());
		}

		if(!toSubscribeList.get(to).contains(from))
			toSubscribeList.get(to).add(from);
		
		if(!toPublishList.get(from).contains(to))
			toPublishList.get(from).add(to);
		
		if(!isDirected)
		{
			if(!toSubscribeList.get(from).contains(to))
				toSubscribeList.get(from).add(to);	
			if(!toPublishList.get(to).contains(from))
				toPublishList.get(to).add(from);
		}
	}

	public ArrayList<Integer> getPublisher(Integer communityID)
	{
		return toSubscribeList.get(communityID);
	}

	public ArrayList<Integer> getSubscribers(Integer communityID)
	{
		return toPublishList.get(communityID);
	}

	public int getPublisherSize(Integer communityID)
	{
		return toSubscribeList.get(communityID).size();
	}

	@Override
	public String toString() {
		return "[Subscriber List "+toSubscribeList+"] [Publisher List "+toPublishList+"]";
	}
}
