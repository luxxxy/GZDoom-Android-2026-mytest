#include "Button.h"
#include "TouchControlsConfig.h"
#include <math.h>

#ifdef __IOS__
#include <sys/time.h>
#endif

#define REPEAT_START_TIME 500
#define REPEAT_INTERVAL   150


using namespace touchcontrols;

Button::Button(std::string tag, RectF pos, std::string image_filename, int value_, bool repeat_, bool hidden_, std::string description_, uint32_t color_) :
        ControlSuper(TC_TYPE_BUTTON, tag, pos)
{
    value = value_;
    image = image_filename;
    description = description_;
    glTex[0] = -1;
    glTex[1] = -1;
    glTexBackdrop = -1;
    id = -1;
    repeat = repeat_;
    hidden = hidden_;
    color = color_;
    updateSize();

    flash = false;
    flashDir = false;
    flashCount = 0;

    drawAlpha = 1.0f; // multiplier: 1.0 leaves the TouchControls' alpha unchanged
}

void Button::updateSize()
{
    glRect.resize(controlPos.right - controlPos.left, controlPos.bottom - controlPos.top);
    glBackdropRect.resize(glRect.width * 1.18f, glRect.height * 1.18f);
}

void Button::setFlash(bool v)
{
    flash = v;
}

void Button::setImage(int32_t i)
{
    if(i < 2)
        glTexDraw = i;
    else
        glTexDraw = 0;
}

void Button::setAlpha(float a)
{
    drawAlpha = a;
}

void Button::resetOutput()
{
    id = -1;
    signal_buttonUp.emit(value);
    signal_button.emit(0, value);
}

bool Button::processPointer(int action, int pid, float x, float y)
{
    if(hidden)  //Hidden controls do not respond to inputs
        return false;

    if(value == -1)
        return false;

    if(action == P_DOWN)
    {
        if(controlPos.contains(x, y))
        {
            id = pid;
            signal_buttonDown.emit(value);
            signal_button.emit(1, value);
            repeatTime = getMS() + REPEAT_START_TIME;//Wait before repeating
            return true;
        }

        return false;
    }
    else if(action == P_UP)
    {
        if(pid == id)
        {
            id = -1;
            signal_buttonUp.emit(value);
            signal_button.emit(0, value);
            return true;
        }

        return false;
    }
    else if(action == P_MOVE)
    {
        return false;
    }

    return false;
}

bool Button::initGL()
{
    std::vector <std::string> images;

    std::istringstream iss(image);

    for(std::string token; std::getline(iss, token, ';');)
    {
        images.push_back(std::move(token));
    }

    int x, y;

    if(images.size() > 0)
    {
        glTex[0] = loadTextureFromPNG(images[0], x, y);
    }

    if(images.size() > 1)
    {
        glTex[1] = loadTextureFromPNG(images[1], x, y);
    }

    return false;
}

bool Button::drawGL(bool forEditor)
{
    if(forEditor)
    {
        if(!hidden)
        {
            gl_drawRect(glTex[0], controlPos.left, controlPos.top, glRect);
        }
    }
    else //Draw normal in game
    {
        if(!hidden)
        {
            if(flash)
            {
                if(getMS() > flashCount)
                {
                    flashCount = getMS() + 300;
                    flashDir = !flashDir;
                }

                if(flashDir)
                    return false;
            }

            // Per-button alpha multiplier (e.g. dim an unavailable action). The
            // owning TouchControls already set the colour/alpha; scale that alpha,
            // preserving the RGB. 1.0 (default) leaves it untouched.
            GLfloat drawR = 1.0f, drawG = 1.0f, drawB = 1.0f, drawA = 1.0f;
            gl_getColor(drawR, drawG, drawB, drawA);

            // Machined steel bezels with a restrained Doom-red combat accent.
            // Original icon textures remain the primary visual cue.
            const bool weaponSlot = tag == "weapon0" || tag == "weapon1" ||
                tag == "weapon2" || tag == "weapon3" || tag == "weapon4" ||
                tag == "weapon5" || tag == "weapon6" || tag == "weapon7" ||
                tag == "weapon8" || tag == "weapon9";
            const bool hudButton = tag == "back" || tag == "attack" ||
                tag == "attack2" || tag == "use" || tag == "jump" ||
                tag == "map" || tag == "gamma" || tag == "use_inventory" ||
                tag == "show_weapons" || tag == "next_weapon" ||
                tag == "prev_weapon" || weaponSlot;
            const float buttonAlpha = drawA * drawAlpha;
            if(hudButton)
            {
                static GLLines hudBezel(56);
                const float radius = (glRect.width < glRect.height ? glRect.width : glRect.height) * 0.49f;
                const float innerRadius = radius * 0.82f;
                const float cx = controlPos.left + glRect.width * 0.5f;
                const float cy = controlPos.top + glRect.height * 0.5f;
                const float tau = 6.28318530718f;
                for(int n = 0; n < 24; ++n)
                {
                    const float a0 = tau * (float)n / 24.0f;
                    const float a1 = tau * (float)(n + 1) / 24.0f;
                    const int outer = n * 6;
                    const int inner = (24 + n) * 6;
                    hudBezel.vertices[outer + 0] = cosf(a0) * radius;
                    hudBezel.vertices[outer + 1] = sinf(a0) * radius;
                    hudBezel.vertices[outer + 2] = 0.0f;
                    hudBezel.vertices[outer + 3] = cosf(a1) * radius;
                    hudBezel.vertices[outer + 4] = sinf(a1) * radius;
                    hudBezel.vertices[outer + 5] = 0.0f;
                    hudBezel.vertices[inner + 0] = cosf(a0) * innerRadius;
                    hudBezel.vertices[inner + 1] = sinf(a0) * innerRadius;
                    hudBezel.vertices[inner + 2] = 0.0f;
                    hudBezel.vertices[inner + 3] = cosf(a1) * innerRadius;
                    hudBezel.vertices[inner + 4] = sinf(a1) * innerRadius;
                    hudBezel.vertices[inner + 5] = 0.0f;
                }
                for(int n = 0; n < 8; ++n)
                {
                    const float a = tau * (float)n / 8.0f;
                    const int v = (48 + n) * 6;
                    hudBezel.vertices[v + 0] = cosf(a) * radius * 0.88f;
                    hudBezel.vertices[v + 1] = sinf(a) * radius * 0.88f;
                    hudBezel.vertices[v + 2] = 0.0f;
                    hudBezel.vertices[v + 3] = cosf(a) * radius * 0.98f;
                    hudBezel.vertices[v + 4] = sinf(a) * radius * 0.98f;
                    hudBezel.vertices[v + 5] = 0.0f;
                }

                const bool pressed = (id != -1);
                const bool fireControl = tag == "attack" || tag == "attack2";
                if(fireControl)
                    gl_color4f(0.78f, 0.16f, 0.12f, buttonAlpha * (pressed ? 0.88f : 0.32f));
                else
                    gl_color4f(0.70f, 0.72f, 0.74f, buttonAlpha * (pressed ? 0.78f : 0.28f));
                gl_drawLines(cx, cy, hudBezel);
                gl_color4f(drawR, drawG, drawB, drawA);

                // Keep glyphs crisp and neutral; only the firing bezel carries red.
                if(fireControl)
                    gl_color4f(0.98f, 0.86f, 0.82f, buttonAlpha);
                else
                    gl_color4f(0.88f, 0.90f, 0.92f, buttonAlpha);
            }
            else if(drawAlpha != 1.0f)
            {
                gl_color4f(drawR, drawG, drawB, buttonAlpha);
            }

            gl_drawRect(glTex[glTexDraw], controlPos.left, controlPos.top, glRect);
            // Avoid leaking per-button tint or alpha into the next control draw.
            gl_color4f(drawR, drawG, drawB, drawA);
        }
    }

    if(repeat)
    {
        if(id != -1)
        {
            if(getMS() > repeatTime)
            {
                repeatTime = getMS() + REPEAT_INTERVAL;

                signal_buttonUp.emit(value);
                signal_button.emit(0, value);

                signal_buttonDown.emit(value);
                signal_button.emit(1, value);
            }
        }
    }

    return false;
}

void Button::saveXML(TiXmlDocument &doc)
{
    TiXmlElement *root = new TiXmlElement(tag.c_str());
    doc.LinkEndChild(root);

    ControlSuper::saveXML(*root);
}

void Button::loadXML(TiXmlDocument &doc)
{
    TiXmlHandle hDoc(&doc);
    TiXmlElement *pElem = hDoc.FirstChild(tag).Element();

    if(!pElem)  //Check exists, if not just leave as default
        return;

    ControlSuper::loadXML(*pElem);
}
