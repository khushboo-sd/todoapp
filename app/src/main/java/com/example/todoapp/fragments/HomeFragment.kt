 package com.example.todoapp.fragments

 import android.app.AlarmManager
 import android.app.PendingIntent
 import android.content.Context
 import android.content.Intent
 import android.os.Bundle
 import android.view.LayoutInflater
 import android.view.View
 import android.view.ViewGroup
 import android.widget.Toast
 import androidx.fragment.app.Fragment
 import androidx.navigation.NavController
 import androidx.navigation.Navigation
 import androidx.recyclerview.widget.LinearLayoutManager
 import com.example.todoapp.databinding.FragmentHomeBinding
 import com.example.todoapp.utils.Notification
 import com.example.todoapp.utils.ToDoAdapter
 import com.example.todoapp.utils.ToDoData
 import com.google.android.material.textfield.TextInputEditText
 import com.google.firebase.auth.FirebaseAuth
 import com.google.firebase.database.DataSnapshot
 import com.google.firebase.database.DatabaseError
 import com.google.firebase.database.DatabaseReference
 import com.google.firebase.database.FirebaseDatabase
 import com.google.firebase.database.ValueEventListener
 import java.util.Calendar


 class HomeFragment : Fragment(),
     AddTodoPopupFragment.DialogNextBtnClickListener,
     ToDoAdapter.ToDoAdapterClicksInterface {

     private lateinit var auth: FirebaseAuth
     private lateinit var databaseRef: DatabaseReference
     private lateinit var navControl: NavController
     private lateinit var binding: FragmentHomeBinding
     private var popUpFragment: AddTodoPopupFragment? = null
     private lateinit var adapter: ToDoAdapter
     private lateinit var mList: MutableList<ToDoData>



     override fun onCreateView(
         inflater: LayoutInflater, container: ViewGroup?,
         savedInstanceState: Bundle?
     ): View {
         binding = FragmentHomeBinding.inflate(inflater, container, false)
         return binding.root
     }

     override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
         super.onViewCreated(view, savedInstanceState)

         init(view)
         getDataFromFirebase()
         registerEvents()
     }

     private fun init(view: View) {
         navControl = Navigation.findNavController(view)

         auth = FirebaseAuth.getInstance()
         databaseRef = FirebaseDatabase.getInstance()
             .reference
             .child("Tasks")
             .child(auth.currentUser!!.uid)

         mList = mutableListOf()
         adapter = ToDoAdapter(mList)
         adapter.setListener(this)

         binding.mainRecyclerView.apply {
             setHasFixedSize(true)
             layoutManager = LinearLayoutManager(context)
             adapter = this@HomeFragment.adapter
         }
     }

     private fun getDataFromFirebase() {
         databaseRef.addValueEventListener(object : ValueEventListener {

             override fun onDataChange(snapshot: DataSnapshot) {
                 mList.clear()

                 for (taskSnapshot in snapshot.children) {
                     val todo = taskSnapshot.key?.let {
                         ToDoData(it, taskSnapshot.value.toString())
                     }
                     if (todo != null) {
                         mList.add(todo)
                     }
                 }

                 adapter.notifyDataSetChanged()
             }

             override fun onCancelled(error: DatabaseError) {
                 Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
             }
         })
     }

     private fun registerEvents() {
         binding.addTaskBtn.setOnClickListener {

             // Close existing popup if open
             popUpFragment?.dismiss()

             popUpFragment = AddTodoPopupFragment()
             popUpFragment?.setListener(this)
             popUpFragment?.show(
                 childFragmentManager,
                 AddTodoPopupFragment.TAG
             )
         }
     }

     override fun onSaveTask(todo: String,date: String, time: String, todoEt: TextInputEditText ) {

         databaseRef.push().setValue(todo).addOnCompleteListener {
             if (it.isSuccessful) {

                 Toast.makeText(context, "Todo Saved Successfully", Toast.LENGTH_SHORT).show()
                 scheduleNotification(todo,date,time)
             } else {
                 Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
             }

             todoEt.text = null
             popUpFragment?.dismiss()
         }
     }

     /*

     override fun onSaveTask(todo: String, todoEt: TextInputEditText) {

         databaseRef.push().setValue(todo).addOnCompleteListener {
             if (it.isSuccessful) {
                 //scheduleNotification(requireContext(), todo, dateTime)
                 Toast.makeText(context, "Todo Saved Successfully", Toast.LENGTH_SHORT).show()
             } else {
                 Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
             }

             todoEt.text = null
             popUpFragment?.dismiss()
         }
     }*/

     override fun onUpdateTask(toDoData: ToDoData, date: String, time: String, todoEt: TextInputEditText) {

         databaseRef.child(toDoData.taskId).setValue(toDoData.task)
             .addOnCompleteListener {

                 if (it.isSuccessful) {
                     Toast.makeText(context, "Updated Successfully", Toast.LENGTH_SHORT).show()
                 } else {
                     Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
                 }

                 todoEt.text = null
                 popUpFragment?.dismiss()
             }
     }



     private fun scheduleNotification(todo: String, date: String, time: String) {
         val calendar = Calendar.getInstance()

         val dateParts = date.split("/")
         val timeParts = time.split(":")

         calendar.set(
             dateParts[2].toInt(), // year
             dateParts[1].toInt() - 1, // month
             dateParts[0].toInt(), // day
             timeParts[0].toInt(), // hour
             timeParts[1].toInt(), // minute
             0
         )

         val intent = Intent(requireContext(), Notification::class.java).apply {
             putExtra("title", "ToDo Reminder")
             putExtra("message", todo)
         }

         val pendingIntent = PendingIntent.getBroadcast(
             requireContext(),
             System.currentTimeMillis().toInt(),
             intent,
             PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
         )

         val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

         // Only check canScheduleExactAlarms on API 31+
         if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) { // S = API 31
             if (alarmManager.canScheduleExactAlarms()) {
                 alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
             } else {
                 Toast.makeText(
                     context,
                     "Exact alarms are not allowed. Please enable permission in settings.",
                     Toast.LENGTH_LONG
                 ).show()
             }
         } else {
             // For API < 31, just setExact normally
             alarmManager.setExact(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
         }
     }


     override fun onDeleteTaskBtnClicked(toDoData: ToDoData) {

         databaseRef.child(toDoData.taskId).removeValue().addOnCompleteListener {
             if (it.isSuccessful) {
                 Toast.makeText(context, "Deleted Successfully", Toast.LENGTH_SHORT).show()
             } else {
                 Toast.makeText(context, it.exception?.message, Toast.LENGTH_SHORT).show()
             }
         }
     }

     override fun onEditTaskBtnClicked(toDoData: ToDoData) {

         popUpFragment?.dismiss()

         popUpFragment = AddTodoPopupFragment.newInstance(toDoData.taskId, toDoData.task)
         popUpFragment?.setListener(this)
         popUpFragment?.show(childFragmentManager, AddTodoPopupFragment.TAG)
     }



 }


