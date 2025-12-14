package com.example.rently.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SeenShowingRepository : JpaRepository<SeenShowing, String>
