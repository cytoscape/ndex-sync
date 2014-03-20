package top;

import java.util.List;

import org.junit.Test;
import org.ndexbio.model.object.Network;
import org.ndexbio.model.tools.ObjectModelTools;
import org.ndexbio.rest.NdexRestClient;
import org.ndexbio.rest.NdexRestClientModelAccessLayer;
import org.ndexbio.rest.NdexRestClientUtilities;

import com.fasterxml.jackson.databind.JsonNode;


public class Copier {
	private static NdexRestClient client = new NdexRestClient("dexterpratt", "insecure");
    private static NdexRestClientModelAccessLayer mal = new NdexRestClientModelAccessLayer(client);

  
    public void testCopyBELNetworkInBlocks() throws IllegalArgumentException, Exception
    {
    	int edgesPerBlock = 100;
    	int nodesPerBlock = 100;
        try
        {
            List<Network> networks = mal.findNetworks("BEL Framework Three Citation Corpus Document");
            
            Network network = networks.get(0);
            
            if (null != network)
            	copyNetworkInBlocks(network, edgesPerBlock, nodesPerBlock);
            
            // Get the target network stats
            
            // Stats should be equal
        
        }
        catch (Exception e)
        {

            e.printStackTrace();
        }
    }
    
	public void testGet(){
		try {
            List<Network> networks = mal.findNetworks("BEL Framework Three Citation Corpus Document");
            
            Network network = networks.get(0);
            
			String route = "/networks/" + network.getId() + "/edges/" + 0 + "/" + 10; 
			JsonNode result = client.get(route, "");
			System.out.println("Network as JSON: " + result.get("name").asText());
			
			Network currentSubnetwork = mal.getEdges(network.getId(), 0, 10);
			System.out.println("Network as Object Model: " + currentSubnetwork.getName());
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    public void copyNetworkInBlocks(Network network, int edgesPerBlock, int nodesPerBlock) throws IllegalArgumentException, Exception{
    	Network currentSubnetwork = null;
    	
    	int skipBlocks = 0;
    	
    	// Get the first block of edges from the source network
    	System.out.println("Getting " + edgesPerBlock + " at offset " + skipBlocks);
    	currentSubnetwork = mal.getEdges(network.getId(), skipBlocks, edgesPerBlock);
    	
    	currentSubnetwork.setName(currentSubnetwork.getName() + " - copy " + Math.random());
    	currentSubnetwork.setMembers(null);
    	
    	ObjectModelTools.summarizeNetwork(currentSubnetwork);
    	
    	// Create the target network
    	System.out.println("Creating network with " + currentSubnetwork.getEdgeCount()  + " edges");
    	Network targetNetwork = mal.createNetwork(currentSubnetwork);

    	String targetNetworkId = targetNetwork.getId();
    	
 
    	// Loop getting subnetworks by edges until the returned subnetwork has no edges
    	do { 
    		skipBlocks++;
    		System.out.println("Getting " + edgesPerBlock + " at offset " + skipBlocks);
    		currentSubnetwork = mal.getEdges(network.getId(), skipBlocks, edgesPerBlock);
    		// Add the subnetwork to the target
    		System.out.println("Adding " + currentSubnetwork.getEdgeCount()  + " edges to network " + targetNetworkId);
    		ObjectModelTools.summarizeNetwork(currentSubnetwork);
    		if (currentSubnetwork.getEdgeCount() > 0) 
    			mal.addNetwork(targetNetworkId, "JDEX_ID", currentSubnetwork);
    	} while (currentSubnetwork.getEdgeCount() > 0);
    	
    	skipBlocks = -1;
    	// Loop getting subnetworks by nodes not in edges until the returned subnetwork has no more nodes
    	do { 
    		skipBlocks++;
    		System.out.println("Getting " + nodesPerBlock + " at offset " + skipBlocks);
    		currentSubnetwork = mal.getNetworkByNonEdgeNodes(network.getId(), skipBlocks, nodesPerBlock);
    		// Add the subnetwork to the target
    		System.out.println("Adding " + currentSubnetwork.getNodeCount()  + " nodes to network " + targetNetworkId);
    		ObjectModelTools.summarizeNetwork(currentSubnetwork);
    		if (currentSubnetwork.getNodeCount() > 0) 
    			mal.addNetwork(targetNetworkId, "JDEX_ID", currentSubnetwork);
    	} while (currentSubnetwork.getNodeCount() > 0);
    			
    }

}
