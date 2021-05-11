var slideIndex = 1;

function plusSlides(e) {
    showSlides(slideIndex += e)
}

function currentSlide(e) {
    showSlides(slideIndex = e)
}

function showSlides(e) {
    var s, l = document.getElementsByClassName("slide"),
        d = document.getElementsByClassName("dot"),
        n = document.getElementsByClassName("sub-image-container");
    for (e > l.length && (slideIndex = 1), e < 1 && (slideIndex = l.length), s = 0; s < l.length; s++) n[s].style.display = "none", l[s].style.display = "none";
    for (s = 0; s < d.length; s++) d[s].className = d[s].className.replace(" active-img", "");
    l[slideIndex - 1].style.display = "block", n[slideIndex - 1].style.display = "block", d[slideIndex - 1].className += " active-img"
}

showSlides(slideIndex);

$( ".image-swipeable" ).on( "swipeleft", function(event){
    plusSlides(1);
});

$( ".image-swipeable" ).on( "swiperight", function(event){
    plusSlides(-1);
});