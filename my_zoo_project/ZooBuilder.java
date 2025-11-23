package my_zoo_project;

import java.util.ArrayList;

class Animal {
    String name;
    String species;
    int age;
    
    Animal(){
        // Your code here
        this.name = "Unnamed";
        this.species = "Unknown";
        this.age = 0;
    }
    
    Animal(String name, String species, int age){
        // Your code here
        this.name = name;  
        this.species = species;
        this.age = age;
    }
     
    // This is a method that any Animal object can call to print its details
    public void printInfo() {
        System.out.println(this.name + ", " + this.species + ", " + this.age);
    }
}

class Panda extends Animal {
    Panda() {
        // Call the parent constructor with default Panda values
        super("Spot", "Panda", 0);
    }
    
    Panda(String name, int age) {
        // Call the parent constructor with the provided values
        super(name, "Panda", age);
    }
}

class Elephant extends Animal {
    Elephant() {
        super("Elle", "Elephant", 0);
    }
    
    Elephant(String name, int age) {
        super(name, "Elephant", age);
    }
}

// Let's add a couple more animal types to make it more interesting!
class Lion extends Animal {
    Lion() {
        super("Simba", "Lion", 0);
    }
    
    Lion(String name, int age) {
        super(name, "Lion", age);
    }
}

class Monkey extends Animal {
    Monkey() {
        super("George", "Monkey", 0);
    }
    
    Monkey(String name, int age) {
        super(name, "Monkey", age);
    }
}

class Giraffe extends Animal {
    Giraffe() {
        super("Stretch", "Giraffe", 0);
    }
    
    Giraffe(String name, int age) {
        super(name, "Giraffe", age);
    }
}

class Zoo {
    // Member variable: a list that can hold Animal objects
    ArrayList<Animal> animals;

    // Constructor for Zoo - initializes our empty list
    public Zoo() {
        this.animals = new ArrayList<Animal>();
    }

    // Method to add an animal to the zoo
    public void addAnimal(Animal a) {
        this.animals.add(a); // Add the animal to our list
    }

    // Method to print info for all animals in the zoo
    public void printAllInfo() {
        System.out.println("All animals in the zoo:");
        
        // This is a for-each loop - it goes through each animal in the list
        for (Animal animal : this.animals) {
            animal.printInfo(); // Call each animal's printInfo method
        }
    }
}

public class ZooBuilder {
    public static void main(String[] args){
        // Step 1: Create a Zoo object
        Zoo myZoo = new Zoo();
        
        // Step 2: Create five animals using your child classes
        // Using parameterized constructors for some, default for others
        
        Panda panda1 = new Panda("Po", 5);
        Elephant elephant1 = new Elephant("Dumbo", 8);
        Lion lion1 = new Lion("Scar", 12);
        Monkey monkey1 = new Monkey("Boots", 3);
        Giraffe giraffe1 = new Giraffe("Melman", 15);
        
        // Step 3: Add all animals to the zoo
        myZoo.addAnimal(panda1);
        myZoo.addAnimal(elephant1);
        myZoo.addAnimal(lion1);
        myZoo.addAnimal(monkey1);
        myZoo.addAnimal(giraffe1);
        
        // Step 4: add one more using default constructor
        Panda defaultPanda = new Panda(); // This will be "Spot", 0 years old
        myZoo.addAnimal(defaultPanda);
        
        // Step 5: Print information for all animals in the zoo
        myZoo.printAllInfo();
        
        // Bonus: Let's see how many animals we have
        System.out.println("\nTotal animals in zoo: " + myZoo.animals.size());
    }
}

