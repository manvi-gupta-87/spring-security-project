import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { Auth } from '../auth/auth';
import { Router } from '@angular/router';

@Component({
  selector: 'app-home',
  imports: [CommonModule],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit{
  userName = '';
  isLoading: boolean = false;

  constructor(private auth:Auth, private router:Router) {}

  ngOnInit(): void {
    
    // Guard ensures user is logged in before reaching here                                                  │ │
    // No need to check authentication anymore  
    
    // if logged in, show user details
      this.userName = 'User';

  }

  logout(): void {
    this.isLoading = true;
    this.auth.logout().subscribe({
      next: () => {
        this.router.navigate(['/login']);
      }, 
      error: (err) => {
        console.error('Logout failed', err);
        this.isLoading = false;
      }
    });
  }

}
