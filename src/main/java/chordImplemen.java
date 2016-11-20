import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by singsand on 11/20/2016.
 */
public class chordImplemen {
    List<node_details> list_nodes=new ArrayList<node_details>();

    public static void main(String arg[])

    {
        System.out.println("Enter first node id to enter ring");

        Scanner in = new Scanner(System.in);
        int num = in.nextInt();
        chordImplemen mainObj=new chordImplemen();
        mainObj.create_new_ring(num);
        mainObj.userInput();








    }
     void create_new_ring(int num){
        node_details obj=new node_details();
        obj.id=num;
        obj.predecessor=-1;
        obj.successor=num;
         //obj.finger_table[0][0]=1;obj.finger_table[0][1]=0;
         //obj.finger_table[1][0]=2;obj.finger_table[1][1]=0;
         //obj.finger_table[2][0]=4;obj.finger_table[2][1]=0;
         obj.fix_fingers();
        list_nodes.add(obj);
         System.out.println("Displaying existing nodes with tables. ");
         display_all_existing_nodes();


    }
    void userInput(){
        while(true) {
            node_details existing_node_j=list_nodes.get(0);
            System.out.println("Enter next node id to enter ring. ");
            Scanner in = new Scanner(System.in);

            int num = in.nextInt();
            node_details obj=new node_details();
            obj=obj.join_ring(num,existing_node_j);

            obj.stabilize();
            node_details successor_obj=new node_details();
            successor_obj=get_node(obj.successor);
            successor_obj.notify(obj);
            list_nodes.get(0).stabilize();
            obj.fix_fingers();
            list_nodes.add(obj);


//            stabilize_fixfingers_checkpredecessor();
//            stabilize_fixfingers_checkpredecessor();
//            stabilize_fixfingers_checkpredecessor();
            System.out.println("Displaying existing nodes with tables. ");
            display_all_existing_nodes();




        }
    }
    void display_all_existing_nodes(){
        for(int i=0;i<list_nodes.size();i++)
        {

                System.out.println("Displaying finger table for Node "+list_nodes.get(i).id);
            System.out.println(            list_nodes.get(i).finger_table[0][0]+":"+list_nodes.get(i).finger_table[0][1]);
            System.out.println(            list_nodes.get(i).finger_table[1][0]+":"+list_nodes.get(i).finger_table[1][1]);
            System.out.println(            list_nodes.get(i).finger_table[2][0]+":"+list_nodes.get(i).finger_table[2][1]);

        }


    }

    void stabilize_fixfingers_checkpredecessor()
    {
        for(int i=0;i<list_nodes.size();i++)
        {
            list_nodes.get(i).stabilize();
            list_nodes.get(i).fix_fingers();
        }

        /*iCheck_Predecessor: // executed periodically to verify whether
        // predecessor still exists
        (6a) if predecessor has failed then
        (6b) predecessor ←−⊥.
        */
    }
    node_details get_node(int id){
        node_details obj=new node_details();
        for(int i=0;i<list_nodes.size();i++)
        {
            if(list_nodes.get(i).id==id)
                return list_nodes.get(i);
        }
        return obj;
    }
}




class node_details {
    int id;
    int successor;
    int predecessor;
    int[][] finger_table = new int[3][2];
    int next_finger = 1;

    node_details join_ring(int num, node_details existing_node_j) {

        node_details obj_i = new node_details();
        obj_i.id = num;
        obj_i.predecessor = -1;

        obj_i.successor = existing_node_j.Locate_Successor(obj_i.id);
        System.out.println("i"+obj_i.id+"i.successor"+obj_i.successor);
        return obj_i;
    }

    int Locate_Successor(int key) {
        int i = this.id;
        int successor = this.successor;
        node_details j = new node_details();
        System.out.println("i:"+i);
        System.out.println("successor:"+successor);
        System.out.println("key.id:"+key);

        if ((key > i && key <= successor) || (!(key <= i && key > successor)) || (key == successor && key != i))
            return successor;

        else

            j = closest_preceding_node(key);

        return (j.Locate_Successor(key));

    }

    node_details closest_preceding_node(int key) {
        int count = 0;
        for (count = 3; count > 0; count--) {
            if ((this.finger_table[count][1] > this.id && this.finger_table[count][1] <= key)
                    || (!(this.finger_table[count][1] <= this.id && this.finger_table[count][1] > key))
                    || (this.id == key && this.finger_table[count][1] != this.id))
                break;
        }
        return (new chordImplemen().get_node((this.finger_table[count][1])));


    }
    void stabilize(){

        int successor=this.successor;
        node_details successor_obj=new chordImplemen().get_node(successor);
        int x=successor_obj.predecessor;
        if((x > this.id && x < successor) || (!(x <= this.id && x >= successor)) || (x == successor && x != this.id)) {
            successor = x;
        }
        successor_obj=new chordImplemen().get_node(successor);
        successor_obj.notify(this);

    }
    void notify(node_details node_j)
    {
        if(this.predecessor==-1 ||
                ((node_j.id > this.predecessor && node_j.id < this.id) || (!(node_j.id <= this.predecessor && node_j.id >= this.id))
                        || (this.predecessor == this.id && node_j.id != this.predecessor)))
        {
            //transfer keys
            this.predecessor=node_j.id;
        }



    }
    void fix_fingers() {
        System.out.println("this.next_finger " + this.next_finger);
        while (true) {
            this.next_finger++;
            if (this.next_finger > 3) {
                this.next_finger = 1;
            }
            this.finger_table[this.next_finger - 1][0] = this.id + (int) Math.pow(2, this.next_finger - 1);
            this.finger_table[this.next_finger - 1][1] = this.Locate_Successor( (int) (Math.pow(2, this.next_finger - 1)));


            if (this.next_finger == 1)
                break;

        }
    }
    }

