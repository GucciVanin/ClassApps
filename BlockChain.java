import java.lang.reflect.Array;
import java.util.*;

/* Block Chain should maintain only limited block nodes to satisfy the functions
   You should not have the all the blocks added to the block chain in memory 
   as it would overflow memory
 */

public class BlockChain {
   public static final int CUT_OFF_AGE = 10;
   private static final int MAX_HEIGHT = 25;

   private LinkedList<Block> chain;
   private LinkedList<BlockNode> chainNode;
   private TransactionPool txPool;

   // all information required in handling a block in block chain
   private class BlockNode {
      public Block b;
      public BlockNode parent;
      public ArrayList<BlockNode> children;
      public int height;
      // utxo pool for making a new block on top of this block
      private UTXOPool uPool;

      public BlockNode(Block b, BlockNode parent, UTXOPool uPool) {
         this.b = b;
         this.parent = parent;
         children = new ArrayList<BlockNode>();
         this.uPool = uPool;
         if (parent != null) {
            height = parent.height + 1;
            parent.children.add(this);
         } else {
            height = 1;
         }
      }

      public UTXOPool getUTXOPoolCopy() {
         return new UTXOPool(uPool);
      }
   }

   /* create an empty block chain with just a genesis block.
    * Assume genesis block is a valid block
    */
   public BlockChain(Block genesisBlock) {
      // IMPLEMENT THIS
      chain = new LinkedList<Block>();
      chain.add(genesisBlock);

      chainNode = new LinkedList<BlockNode>();
      BlockNode genesisNode = new BlockNode(genesisBlock, null, getMaxHeightUTXOPool());
      chainNode.add(genesisNode);

      txPool = new TransactionPool();
   }

   /* Get the maximum height block
    */
   public Block getMaxHeightBlock() {
      // IMPLEMENT THIS
      return chain.getLast();
   }
   
   /* Get the UTXOPool for mining a new block on top of 
    * max height block
    */
   public UTXOPool getMaxHeightUTXOPool() {
      // IMPLEMENT THIS
      UTXOPool temp = chainNode.getLast().getUTXOPoolCopy();
      return temp;
   }
   
   /* Get the transaction pool to mine a new block
    */
   public TransactionPool getTransactionPool() {
      // IMPLEMENT THIS
      return txPool;
   }

   /* Add a block to block chain if it is valid.
    * For validity, all transactions should be valid
    * and block should be at height > (maxHeight - CUT_OFF_AGE).
    * For example, you can try creating a new block over genesis block 
    * (block height 2) if blockChain height is <= CUT_OFF_AGE + 1. 
    * As soon as height > CUT_OFF_AGE + 1, you cannot create a new block at height 2.
    * Return true of block is successfully added
    */
   public boolean addBlock(Block b) {
       // IMPLEMENT THIS
      TxHandler handler = new TxHandler(getMaxHeightUTXOPool());

      for(Transaction tx : b.getTransactions()){
         handler.isValidTx(tx);
      }
      
      if(b.getPrevBlockHash() == null) return false;

      BlockNode bNode = new BlockNode(b, this.chainNode.getLast(), this.getMaxHeightUTXOPool());

      int currentBlockHeight = chainNode.getLast().height / chain.size();
      if (currentBlockHeight == bNode.height ) return false;

      chain.add(b);
      for(BlockNode node : bNode.children){
         chainNode.add(node);
      }

      return true;
   }

   /* Add a transaction in transaction pool
    */
   public void addTransaction(Transaction tx) {
      // IMPLEMENT THIS
      txPool.addTransaction(tx);
   }
}
