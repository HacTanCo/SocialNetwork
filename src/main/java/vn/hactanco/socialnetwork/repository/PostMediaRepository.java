package vn.hactanco.socialnetwork.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import vn.hactanco.socialnetwork.model.PostMedia;

@Repository
public interface PostMediaRepository extends JpaRepository<PostMedia, Long> {

}