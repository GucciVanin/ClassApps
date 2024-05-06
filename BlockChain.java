import java.sql.Timestamp;
import java.util.*;

/* Block Chain should maintain only limited block nodes to satisfy the functions
   You should not have the all the blocks added to the block chain in memory 
   as it would overflow memory
 */

public class BlockChain {
   public static final int CUT_OFF_AGE = 10;

   private ArrayList<BlockNode> bNode = new ArrayList<>();
   private TransactionPool txPool = new TransactionPool();
   private BlockNode hNode;

   public void updateMaxHeightNode() {
      BlockNode currentMaxHeightNode = hNode;
      for (BlockNode b : bNode) {
          if (b.height > currentMaxHeightNode.height) {
              currentMaxHeightNode = b;
          } else if (b.height == currentMaxHeightNode.height) {
              if (currentMaxHeightNode.tStamp.after(b.tStamp)) {
                  currentMaxHeightNode = b;
              }
          }
      }
      hNode = currentMaxHeightNode;
   }
  
   public BlockNode getParentNode(byte[] blockNode){
      ByteArrayWrapper b1 = new ByteArrayWrapper(blockNode);
        for (BlockNode _b : bNode) {
            ByteArrayWrapper b2 = new ByteArrayWrapper(_b.b.getHash());
            if (b1.equals(b2)) {
                return _b;
            }
        }
        return null;
   }

   // all information required in handling a block in block chain
   private class BlockNode {
      public Block b;
      public int height;
      // utxo pool for making a new block on top of this block
      private UTXOPool uPool;
      private TransactionPool _txPool;
      private Timestamp tStamp;

      public BlockNode(Block b, int height, UTXOPool uPool, TransactionPool txPool) {
         this.b = b;
         this.height = height;
         this.uPool = uPool;
         this._txPool = txPool;
         this.tStamp = new Timestamp(System.currentTimeMillis());
         
      }

      public UTXOPool getUTXOPoolCopy() {
         return this.uPool;
      }

      public TransactionPool geTransactionPool(){
         return this._txPool;
      }
   }

   /* create an empty block chain with just a genesis block.
    * Assume genesis block is a valid block
    */
   public BlockChain(Block genesisBlock) {
      // IMPLEMENT THIS
      UTXOPool utxoPool = new UTXOPool();
      TransactionPool transPool = new TransactionPool();
      for (int i = 0; i < genesisBlock.getCoinbase().numOutputs(); i++) {
         utxoPool.addUTXO(new UTXO(genesisBlock.getCoinbase().getHash(),i),genesisBlock.getCoinbase().getOutput(i));
      }
      transPool.addTransaction(genesisBlock.getCoinbase());
      for (Transaction t : genesisBlock.getTransactions()) {
         if (t != null) {
            for (int i=0;i<t.numOutputs();i++) {
               Transaction.Output output = t.getOutput(i);
               UTXO utxo = new UTXO(t.getHash(),i);
               utxoPool.addUTXO(utxo,output);
            }
               transPool.addTransaction(t);
         }
      }
      BlockNode b = new BlockNode(genesisBlock, 1, utxoPool, transPool);
      hNode = b;
      bNode.add(b);
   }

   /* Get the maximum height block
    */
   public Block getMaxHeightBlock() {
      // IMPLEMENT THIS
      return hNode.b;
   }
   
   /* Get the UTXOPool for mining a new block on top of 
    * max height block
    */
   public UTXOPool getMaxHeightUTXOPool() {
      // IMPLEMENT THIS
      return hNode.getUTXOPoolCopy();
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
      //check block is genesisBlock?
      if (b.getPrevBlockHash() == null) {
         return false;
      }
      //check parent Hash

      BlockNode parentNode = getParentNode(b.getPrevBlockHash());
      if(parentNode == null) {
            return false;
      }
      //compare height
      int blockHeight = parentNode.height+1;
      if (blockHeight <= hNode.height - CUT_OFF_AGE) {
            return false;
      }

      //check all transactions in block are valid?
      UTXOPool utxoPool = new UTXOPool(parentNode.getUTXOPoolCopy());

      TransactionPool transPool = new TransactionPool(parentNode.geTransactionPool());
      for (Transaction t : b.getTransactions()) {
            TxHandler txHandler = new TxHandler(utxoPool);
            if (!txHandler.isValidTx(t)) {
               return false;
            }
            //remove used utxo
            for (Transaction.Input input : t.getInputs()) {
               int outputIndex = input.outputIndex;
               byte[] prevTxHash = input.prevTxHash;
               UTXO utxo = new UTXO(prevTxHash, outputIndex);
               utxoPool.removeUTXO(utxo);
            }
            //add new utxo
            byte[] hash = t.getHash();
            for (int i=0;i<t.numOutputs();i++) {
               UTXO utxo = new UTXO(hash, i);
               utxoPool.addUTXO(utxo, t.getOutput(i));
            }
      }

      //update utxo transaction coinbase
      for (int i = 0; i < b.getCoinbase().numOutputs(); i++) {
            utxoPool.addUTXO(new UTXO(b.getCoinbase().getHash(),i),b.getCoinbase().getOutput(i));
      }

      //remove trans pool
      for (Transaction t : b.getTransactions()) {
            transPool.removeTransaction(t.getHash());
      }

      //add new block
      BlockNode temp = new BlockNode(b, blockHeight, utxoPool, transPool);
      boolean addNewBlock = bNode.add(temp);
      if (addNewBlock) {
            updateMaxHeightNode();
      }
      return addNewBlock;
   }

   /* Add a transaction in transaction pool
    */
   public void addTransaction(Transaction tx) {
      // IMPLEMENT THIS
      txPool.addTransaction(tx);
   }
}

