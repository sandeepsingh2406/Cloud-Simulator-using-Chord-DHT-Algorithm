import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by singsand on 11/20/2016.
 */
public class chordImplemen {
    static List<node_details> list_nodes=new ArrayList<node_details>();

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
            list_nodes.add(obj);
            int j=0;
            while(j<3) {
                for (int i = 0; i < list_nodes.size(); i++) {
                    obj.stabilize();
                    list_nodes.get(i).stabilize();
                    obj.fix_fingers();
                    list_nodes.get(i).fix_fingers();

                }
                j++;
            }






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
            System.out.println("predecessor"+list_nodes.get(i).predecessor);
            System.out.println("successor"+list_nodes.get(i).successor);

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

    node_details find_predecessor(int id){
        node_details n_dash = new node_details();
        n_dash=this;

        while(((n_dash.id > n_dash.successor && (id <= n_dash.id && id > n_dash.successor)) ||
                (n_dash.id < n_dash.successor && (id <= n_dash.id || id > n_dash.successor)))
                &&  (n_dash.id!=n_dash.successor ))
        {
            n_dash = n_dash.closest_preceding_finger(id);
        }
        return n_dash;

    }

    int Locate_Successor(int id) {
//        int i = this.id;
//        int successor = this.successor;

        node_details n_dash = new node_details();
        n_dash=this.find_predecessor(id);
        return n_dash.successor;
//        System.out.println("i:"+i);
//        System.out.println("successor:"+successor);
//        System.out.println("key.id:"+key);
//
//        if ((this.id>this.successor && (key > this.id || key <= this.successor)) ||
//                (this.id<this.successor && key > this.id && key <= this.successor)
//                || this.id == this.successor  )
//            return successor;
//
//        else
//
//            j = closest_preceding_node(key);
//
//        return (j.Locate_Successor(key));

    }

    node_details closest_preceding_finger(int id) {
        int count = 0;
        for (count = 3; count > 0; count--) {
            if  ((this.id>id && (this.finger_table[count-1][1] > this.id || this.finger_table[count-1][1] < id))
                        || (this.id<id && this.finger_table[count-1][1] > this.id && this.finger_table[count-1][1] < id)
                        || (this.id==id && this.id!=this.finger_table[count-1][1])  )

                return (new chordImplemen().get_node((this.finger_table[count-1][1])));
        }
        return (this);
//
//
    }
    void stabilize(){

        node_details successor_obj=new chordImplemen().get_node(this.successor);
        int x=successor_obj.predecessor;
        if(     x > -1 &&
                ((this.id>this.successor && (x > this.id || x < this.successor)) || (this.id<this.successor && x > this.id && x < this.successor)
                || this.id == this.successor && x != this.id ))
         {
            this.successor = x;
        }
        successor_obj=new chordImplemen().get_node(this.successor);
        successor_obj.notify(this);

    }
    void notify(node_details node_j)
    {
        if(this.predecessor==-1 ||

                ((this.predecessor>this.id && (node_j.id > this.predecessor || node_j.id < this.id)) ||
                        (this.predecessor>this.id && node_j.id > this.predecessor && node_j.id < this.id)
                        || this.predecessor == this.id && node_j.id != this.predecessor ))

        {
            //transfer keys
            this.predecessor=node_j.id;
        }



    }
    void fix_fingers() {
        System.out.println("this.next_finger " + this.next_finger);
        while (true) {

            if (this.next_finger > 3) {
                this.next_finger = 1;
            }

            int id =(this.id + (int) Math.pow(2, this.next_finger - 1))%(int)Math.pow(2, 3);

            this.finger_table[this.next_finger - 1][0] = id;
            this.finger_table[this.next_finger - 1][1] = this.Locate_Successor(id);

            this.next_finger++;

            if (this.next_finger > 3)
                break;

        }
    }
    }

