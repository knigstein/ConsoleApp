# Console-application-for-managing-collections
A console application that implements interactive management of a collection of objects. The collection must store objects of the StudyGroup class, described below.

---

In interactive mode, the program supports executing the following commands:
    help : output help for available commands
    info : output information about the collection (type, initialization date, number of elements, etc.)
    to the standard output stream show : output all the elements of the collection in a string representation to the standard output stream
    add {element} : add a new element to the collection
    update id {element} : update the value of a collection element whose id is equal to the specified
    remove_by_id id : delete an element from the collection by its id
    clear : clear the collection
    save : save the collection to a file
    execute_script file_name : read and execute the script from the specified file. The script contains commands in the same form as they are entered by the      user interactively.
    exit : terminate the program (without saving to a file)
    remove_first : remove the first element from the collection
    add_if_min {element} : add a new element to the collection if its value is less than that of the smallest element in this collection
    remove_lower {element} : remove from the collection all elements smaller than the specified one
    filter_contains_name name : output the elements whose name field value contains the specified substring
    filter_greater_than_semester_enum semesterEnum : output the elements whose semesterEnum field value is greater than the specified
    one print_field_descending_group_admin : output the values of the GroupAdmin field of all elements in descending order
