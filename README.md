# ThevenApp v1.0 🚀
> **Awarded with Honors (Outstanding) - Final Degree Project**

ThevenApp is a comprehensive desktop application designed for electrical engineering education. It combines a powerful **circuit simulation engine** with a full **Learning Management System (LMS)** to help students master Thévenin and Norton theorems through an intuitive, modern interface.

![Main Interface](screenshots/diseno.jpg)

## 🌟 Key Features

* **Interactive Circuit Designer:** Drag-and-drop components (resistors, independent/dependent sources) with real-time node detection.
* **Theorems Analysis Engine:** Automatic calculation of Thévenin and Norton equivalents with step-by-step summaries.
* **E-Learning Platform:** * **Student Mode:** Practice labs with difficulty levels (Easy, Intermediate) and progress tracking.
    * **Professor Mode:** Full content management system (CMS) with an HTML editor for theory and PDF document management.
* **Data Portability:** Circuits are serialized and saved using **JSON** format for easy sharing.

## 🛠️ Technical Stack

* **Language:** Java 17+
* **Framework:** JavaFX (Modern UI/UX)
* **Build Tool:** Maven
* **Mathematical Engine:** Apache Commons Math3
* **Database:** SQLite (Local persistence)
* **JSON Processing:** Jackson Databind
* **Logging:** Java Util Logging

## 📸 Screenshots

### Circuit Analysis & Equivalent Generation
![Analysis Panel](screenshots/panel-analisis.jpg)
*Visual representation of equivalent circuits with automated calculation reports.*

### E-Learning Lab
![Practice Lab](screenshots/estudiante-ejercicios.jpg)
*Gamified learning environment for students to solve specific circuit challenges.*

### Theory Management (Professor CMS)
![CMS Panel](screenshots/manual-gestion-teoria-prof.jpg)
*Integrated HTML editor for professors to create and manage academic content.*

## 🚀 Installation & Running

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/mgarzon34/ThevenApp.git](https://github.com/mgarzon34/ThevenApp.git)
