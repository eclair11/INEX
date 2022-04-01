#!/usr/bin/perl
use strict ;
no strict 'refs';
use XML::LibXML;

BEGIN {}

my ($repcoll) = "test_coll_M2"; # répertoire ne contenant que des fichiers XXXXX.xml
my ($parser) = XML::LibXML->new('recover'=>1);
$parser->recover_silently(1); # pour éviter que le parser plante si erreur XML

# comptages
my ($nb_articles) = 0;
my ($nb_elements) = 0;
my ($taille_texte) = 0;

# limites
my ($nb_todo) = 100;
my (%balises_ok) = ('bdy'=>1, 'sec'=>1, 'title'=>1);

# on ouvre le répertoire de la collection
opendir(REPIN, $repcoll) || die "Erreur ouverture !\n";
my ($entry);
while ($entry = readdir(REPIN)) {
    if (! ($entry =~ /^\.\.?$/)) {
	# Si le nom de fichier ne commence pas par ".", c'est donc un fichier XML
	$nb_articles ++;
	if (!($nb_articles % 100)) { # Une trace
	    print "Nombre d'articles : $nb_articles\n";
	}
	# On parse le fichier XML
	my ($tree) = $parser->parse_file( "$repcoll/$entry" );
	if ($tree) {
	    # on récupère la racine
	    my $root = $tree->getDocumentElement;
	    if ($root) {
		# on cherche un élément "titre"
		my ($titre) = $root->findvalue('//title');
		$titre =~ s/\s+/ /g;
		$nb_elements ++;
		# on récupère le texte de tout l'article
		my ($texte) = $root->textContent;
		$taille_texte += length($texte);
		my ($chemin) = '/article[1]';
		my (%num_balises);
		# on parcours les fils de la racine
		foreach my $node ($root->childNodes()) {
		    my ($tag) = $node->nodeName();
		    if ($balises_ok{$tag}) {
			$nb_elements ++;
			$num_balises{$tag}++;
			print "$entry$chemin/$tag\[$num_balises{$tag}\] : $titre\n";
		    }
		}
	    } else {
		print "Erreur title parser (fichier $repcoll/$entry\n";
	    }
	} else {
	    print "Erreur parser (fichier $repcoll/$entry\n";
	}
	last if ($nb_articles == $nb_todo);
    }
}



print "Nombre d'articles : $nb_articles
Nombre d'elements : $nb_elements
Taille totale texte : $taille_texte\n";
