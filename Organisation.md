Quynh Vo (qvo117), Michael McKenzie (BlackbirdAB), Min Soo Sohn (Dannycom)

Point #1:
The way we organised the team was that we had regular meetings to discuss the project.
Our team discussed the domain model together while Michael implemented it. 
Michael implemented the mapper classes.
Quynh and Michael made changes to the domain model and mapper classes to improve it.
Michael started on the ConcertResource with the concerts endpoint.
Our team discussed the other endpoints, and we implemented them together on Daniel's laptop.
Michael worked on making the database populate correctly.
Quynh fixed the concerts and login endpoints.
Michael fixed some JPQL syntax errors and worked on the bookings endpoint.
Our team continued to discuss, and Michael fixed the remaining bookings endpoint failures.
Daniel worked on the subscribe endpoint, and we continued to work together.

Point #2:
We minimised the chance of concurrency errors by using optimistic concurrency control on the seats so that two concurrent transactions cannot book the same seats using version control, only the first transaction will book it.
We used synchronisation to protect the subscription lists from being accessed by multiple threads at the same time.

Point #3:
We organised the domain model based on the table names and columns used in db-init.sql, the completed DTO classes, and the project description. 
These resources helped us to decide on which classes and entity associations we wanted to map and which collections we thought were appropriate to use.
We used the default fetch plans for our persistent collections.

A user can make multiple bookings, so @ManyToOne on the Booking class references the user object.
A booking can have multiple seats booked, so @OneToMany on the Booking class.
A concert can be on multiple dates, which is a collection. 
Concert and Performer have a @ManyToMany association.

